package com.zegreatrob.coupling.repository.dynamo

import com.zegreatrob.coupling.repository.dynamo.external.get
import com.zegreatrob.coupling.repository.dynamo.external.put
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.asyncSetup
import kotlinx.coroutines.await
import kotlin.js.Json
import kotlin.js.json
import kotlin.test.Test

class TargetSchemaBootstrapTest {

    @Test
    fun createsTargetTablesAndPreservesTheInitialLedger() = asyncSetup(object {
        val stage = "target-schema-bootstrap-test"
    }) {} exercise {
        val first = TargetSchemaBootstrap.ensure(stage, MigrationMode.DISABLED)
        val second = TargetSchemaBootstrap.ensure(stage, MigrationMode.DISABLED)
        val tables = buildList {
            for (tableName in targetTableNames()) add(describeTable(tableName))
        }
        val ttlStatus = DynamoDbProvider.dynamoDB.describeTimeToLive(
            json("TableName" to "test-coupling-live-connections"),
        ).await()["TimeToLiveDescription"].unsafeCast<Json>()["TimeToLiveStatus"]
        val ledger = DynamoDbProvider.dynamoDBClient.get(
            json(
                "TableName" to "test-coupling-core",
                "Key" to json("PK" to "MIGRATION#$stage", "SK" to "STATE"),
            ),
        ).await()["Item"].unsafeCast<Json>()
        TargetSchemaResult(first, second, tables, ttlStatus, ledger)
    } verify { result: TargetSchemaResult ->
        result.first.assertIsEqualTo(MigrationLedgerState(MigrationLedgerPhase.DISABLED, 1))
        result.second.assertIsEqualTo(result.first)
        result.tables.forEach { table ->
            table["KeySchema"].unsafeCast<Array<Json>>().map { it["AttributeName"] }
                .assertIsEqualTo(listOf("PK", "SK"))
        }
        result.tables.last()["GlobalSecondaryIndexes"].unsafeCast<Array<Json>>().single()["IndexName"]
            .assertIsEqualTo("byConnection")
        result.tables.last()["GlobalSecondaryIndexes"].unsafeCast<Array<Json>>().single()["KeySchema"]
            .unsafeCast<Array<Json>>()
            .map { it["AttributeName"] }
            .assertIsEqualTo(listOf("GSI1PK", "GSI1SK"))
        result.ttlStatus.assertIsEqualTo("ENABLED")
        result.ledger["targetSchemaVersion"].assertIsEqualTo(1)
        result.ledger["phase"].assertIsEqualTo("DISABLED")
        result.ledger["ledgerVersion"].assertIsEqualTo(1)
        result.ledger["requestedMode"].assertIsEqualTo("disabled")
        result.ledger["activeRunId"].assertIsEqualTo(null)
        JSON.stringify(result.ledger["reconciliationSummary"]).assertIsEqualTo("{}")
        result.ledger["cutoverRevision"].assertIsEqualTo(null)
    }

    @Test
    fun doesNotReplaceExistingLedgerProgress() = asyncSetup(object {
        val stage = "target-schema-bootstrap-preserved-ledger"
    }) {} exercise {
        TargetSchemaBootstrap.ensureUncached(stage, MigrationMode.DISABLED)
        DynamoDbProvider.dynamoDBClient.put(
            json(
                "TableName" to "test-coupling-core",
                "Item" to json(
                    "PK" to "MIGRATION#$stage",
                    "SK" to "STATE",
                    "targetSchemaVersion" to 1,
                    "phase" to "BACKFILLING",
                    "ledgerVersion" to 17,
                ),
            ),
        ).await()
        TargetSchemaBootstrap.ensureUncached(stage, MigrationMode.DISABLED)
    } verify { result: MigrationLedgerState ->
        result.assertIsEqualTo(MigrationLedgerState(MigrationLedgerPhase.BACKFILLING, 17))
    }

    private suspend fun describeTable(tableName: String) = DynamoDbProvider.dynamoDB
        .describeTable(json("TableName" to "test-$tableName"))
        .await()["Table"]
        .unsafeCast<Json>()

    private fun targetTableNames() = listOf(
        "coupling-core",
        "coupling-activity",
        "coupling-history",
        "coupling-live-connections",
    )

    private data class TargetSchemaResult(
        val first: MigrationLedgerState,
        val second: MigrationLedgerState,
        val tables: List<Json>,
        val ttlStatus: Any?,
        val ledger: Json,
    )
}
