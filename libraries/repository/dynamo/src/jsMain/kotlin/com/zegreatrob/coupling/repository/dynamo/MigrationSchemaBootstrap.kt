package com.zegreatrob.coupling.repository.dynamo

import com.zegreatrob.coupling.repository.dynamo.external.get
import com.zegreatrob.coupling.repository.dynamo.external.put
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.async
import kotlinx.coroutines.await
import kotlinx.coroutines.yield
import kotlin.js.Json
import kotlin.js.json
import kotlin.time.Clock

enum class MigrationMode(val environmentValue: String) {
    DISABLED("disabled"),
    DUAL_WRITE("dual_write"),
    CUTOVER("cutover"),
    ;

    companion object {
        fun parse(value: String?) = entries.firstOrNull { it.environmentValue == value }
            ?: throw IllegalStateException("Invalid DYNAMODB_SCHEMA_MIGRATION_MODE: $value")
    }
}

enum class MigrationLedgerPhase {
    DISABLED,
    BACKFILLING,
    DUAL_WRITE,
    CUTOVER,
}

data class MigrationLedgerState(
    val phase: MigrationLedgerPhase,
    val ledgerVersion: Int,
)

object TargetSchemaBootstrap : DynamoDBSyntax by DynamoDbProvider {
    private const val TARGET_SCHEMA_VERSION = 1
    private val bootstrapScope = MainScope()
    private val initializedStages = mutableMapOf<String, Deferred<Unit>>()
    private val tables = listOf(
        TargetTableDefinition("coupling-core"),
        TargetTableDefinition("coupling-activity"),
        TargetTableDefinition("coupling-history"),
        TargetTableDefinition("coupling-live-connections", hasConnectionIndex = true, ttlAttribute = "expiresAt"),
    )

    suspend fun ensure(stage: String, requestedMode: MigrationMode): MigrationLedgerState {
        require(stage.isNotBlank()) { "STAGE is required for DynamoDB schema migration" }
        initializedStages.getOrPut("${dynamoPrefix()}:$stage") {
            bootstrapScope.async {
                ensureUncached(stage, requestedMode)
                Unit
            }
        }.await()
        return MigrationLedgerRepository(stage, requestedMode).loadState()
    }

    suspend fun existingState(stage: String): MigrationLedgerState? {
        require(stage.isNotBlank()) { "STAGE is required for DynamoDB schema migration" }
        return try {
            MigrationLedgerRepository(stage, MigrationMode.DISABLED).loadStateOrNull()
        } catch (error: Throwable) {
            if (error.name() == "ResourceNotFoundException") null else throw error
        }
    }

    internal suspend fun ensureUncached(stage: String, requestedMode: MigrationMode): MigrationLedgerState {
        for (table in tables) ensureTable(table)
        return MigrationLedgerRepository(stage, requestedMode).ensureState()
    }

    private suspend fun ensureTable(table: TargetTableDefinition) {
        if (!tableExists(table.name)) {
            createTableIfNeeded(table)
        }
        waitForActive(table.name)
        validateTable(table)
        table.ttlAttribute?.let { enableTtl(table.name, it) }
    }

    private suspend fun createTableIfNeeded(table: TargetTableDefinition) {
        try {
            dynamoDB.createTable(table.createParams()).await()
        } catch (error: Throwable) {
            if (error.name() != "ResourceInUseException") throw error
        }
    }

    private suspend fun validateTable(table: TargetTableDefinition) {
        val tableDescription = dynamoDB.describeTable(json("TableName" to prefixed(table.name)))
            .await()["Table"]
            .unsafeCast<Json>()
        require(JSON.stringify(tableDescription["KeySchema"]) == JSON.stringify(table.keySchema())) {
            "Unexpected key schema for ${table.name}"
        }
        if (table.hasConnectionIndex) {
            val indexes = tableDescription["GlobalSecondaryIndexes"].unsafeCast<Array<Json>?>()
            val connectionIndex = indexes?.singleOrNull { it["IndexName"] == "byConnection" }
            require(connectionIndex != null) {
                "Missing byConnection index for ${table.name}"
            }
            require(JSON.stringify(connectionIndex["KeySchema"]) == JSON.stringify(table.connectionIndexKeySchema())) {
                "Unexpected byConnection index schema for ${table.name}"
            }
        }
    }

    private suspend fun tableExists(tableName: String) = try {
        dynamoDB.describeTable(json("TableName" to prefixed(tableName))).await()
        true
    } catch (_: Throwable) {
        false
    }

    private suspend fun waitForActive(tableName: String) {
        while (tableStatus(tableName) != "ACTIVE") {
            yield()
        }
    }

    private suspend fun tableStatus(tableName: String) = dynamoDB
        .describeTable(json("TableName" to prefixed(tableName)))
        .await()["Table"]
        .unsafeCast<Json>()["TableStatus"]

    private suspend fun enableTtl(tableName: String, attributeName: String) {
        val status = dynamoDB.describeTimeToLive(json("TableName" to prefixed(tableName)))
            .await()["TimeToLiveDescription"]
            .unsafeCast<Json?>()
            ?.get("TimeToLiveStatus")
        if (status == "DISABLED" || status == null) {
            dynamoDB.updateTimeToLive(
                json(
                    "TableName" to prefixed(tableName),
                    "TimeToLiveSpecification" to json(
                        "AttributeName" to attributeName,
                        "Enabled" to true,
                    ),
                ),
            ).await()
        }
    }

    private fun prefixed(tableName: String) = "${dynamoPrefix()}$tableName"

    private data class TargetTableDefinition(
        val name: String,
        val hasConnectionIndex: Boolean = false,
        val ttlAttribute: String? = null,
    ) {
        fun createParams(): Json = json(
            "TableName" to "${dynamoPrefix()}$name",
            "KeySchema" to keySchema(),
            "AttributeDefinitions" to attributeDefinitions(),
            "BillingMode" to "PAY_PER_REQUEST",
        ).also { params ->
            if (hasConnectionIndex) {
                params["GlobalSecondaryIndexes"] = arrayOf(
                    json(
                        "IndexName" to "byConnection",
                        "KeySchema" to connectionIndexKeySchema(),
                        "Projection" to json("ProjectionType" to "KEYS_ONLY"),
                    ),
                )
            }
        }

        fun keySchema() = arrayOf(
            json("AttributeName" to "PK", "KeyType" to "HASH"),
            json("AttributeName" to "SK", "KeyType" to "RANGE"),
        )

        fun connectionIndexKeySchema() = arrayOf(
            json("AttributeName" to "GSI1PK", "KeyType" to "HASH"),
            json("AttributeName" to "GSI1SK", "KeyType" to "RANGE"),
        )

        private fun attributeDefinitions() = buildList {
            add(json("AttributeName" to "PK", "AttributeType" to "S"))
            add(json("AttributeName" to "SK", "AttributeType" to "S"))
            if (hasConnectionIndex) {
                add(json("AttributeName" to "GSI1PK", "AttributeType" to "S"))
                add(json("AttributeName" to "GSI1SK", "AttributeType" to "S"))
            }
        }.toTypedArray()
    }

    private class MigrationLedgerRepository(
        private val stage: String,
        private val requestedMode: MigrationMode,
    ) : DynamoDBSyntax by DynamoDbProvider {
        suspend fun ensureState(): MigrationLedgerState {
            try {
                dynamoDBClient.put(initialStateParams()).await()
            } catch (error: Throwable) {
                if (error.name() != "ConditionalCheckFailedException") throw error
            }
            return loadState()
        }

        suspend fun loadState(): MigrationLedgerState = loadStateOrNull()
            ?: throw IllegalStateException("Migration state was not created for $stage")

        suspend fun loadStateOrNull(): MigrationLedgerState? {
            val item = dynamoDBClient.get(
                json(
                    "TableName" to "${dynamoPrefix()}coupling-core",
                    "Key" to json("PK" to statePartitionKey(), "SK" to "STATE"),
                ),
            ).await()["Item"].unsafeCast<Json?>() ?: return null
            val schemaVersion = item["targetSchemaVersion"]
            check(schemaVersion == TARGET_SCHEMA_VERSION) {
                "Unsupported target schema version for $stage: ${schemaVersion ?: "missing"}"
            }
            return MigrationLedgerState(
                MigrationLedgerPhase.valueOf(item["phase"].toString()),
                item["ledgerVersion"].toString().toInt(),
            )
        }

        private fun initialStateParams(): Json {
            val timestamp = Clock.System.now().toString()
            return json(
                "TableName" to "${dynamoPrefix()}coupling-core",
                "Item" to json(
                    "PK" to statePartitionKey(),
                    "SK" to "STATE",
                    "targetSchemaVersion" to TARGET_SCHEMA_VERSION,
                    "phase" to MigrationLedgerPhase.DISABLED.name,
                    "ledgerVersion" to 1,
                    "requestedMode" to requestedMode.environmentValue,
                    "activeRunId" to null,
                    "reconciliationSummary" to json(),
                    "cutoverRevision" to null,
                    "createdAt" to timestamp,
                    "updatedAt" to timestamp,
                ),
                "ConditionExpression" to "attribute_not_exists(PK)",
            )
        }

        private fun statePartitionKey() = "MIGRATION#$stage"
    }
}

private fun Throwable.name() = asDynamic().name.unsafeCast<String?>()

private fun dynamoPrefix() = js("process.env.DYNAMO_PREFIX").unsafeCast<String?>()
    ?: throw IllegalStateException("No Dynamo Prefix Configured.")
