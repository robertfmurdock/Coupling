package com.zegreatrob.coupling.repository.dynamo

import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.repository.dynamo.external.awslibdynamodb.DynamoConditionCheck
import com.zegreatrob.coupling.repository.dynamo.external.awslibdynamodb.DynamoDeleteItemInput
import com.zegreatrob.coupling.repository.dynamo.external.awslibdynamodb.DynamoKey
import com.zegreatrob.coupling.repository.dynamo.external.awslibdynamodb.DynamoPutItemInput
import com.zegreatrob.coupling.repository.dynamo.external.awslibdynamodb.DynamoQueryInput
import com.zegreatrob.coupling.repository.dynamo.external.awslibdynamodb.GetItemCommandInput
import com.zegreatrob.coupling.repository.dynamo.external.awslibdynamodb.TransactWriteCommandInput
import com.zegreatrob.coupling.repository.dynamo.external.awslibdynamodb.TransactWriteItem
import com.zegreatrob.coupling.repository.dynamo.external.dynamoOptional
import com.zegreatrob.coupling.repository.dynamo.external.getItem
import com.zegreatrob.coupling.repository.dynamo.external.queryItems
import com.zegreatrob.coupling.repository.dynamo.external.transactWrite
import com.zegreatrob.coupling.repository.party.PartyConfigurationBackfill
import com.zegreatrob.coupling.repository.party.PartyConfigurationBackfillResult
import kotlinx.coroutines.await
import kotlin.js.Json
import kotlin.js.json
import kotlin.time.Clock

class DynamoPartyConfigurationBackfill(
    private val dynamoDBSyntax: DynamoDBSyntax = DynamoDbProvider,
    private val pageSize: Int = 100,
) : PartyConfigurationBackfill,
    LegacyPartyConfigurationMapping,
    DynamoDBSyntax by dynamoDBSyntax {

    override suspend fun backfill(stage: String, partyId: PartyId, runId: String): PartyConfigurationBackfillResult {
        require(stage.isNotBlank() && runId.isNotBlank()) { "Stage and run ID are required" }
        require(pageSize in 1..1000) { "Page size must be between 1 and 1000" }
        configuredStage()?.let { require(it == stage) { "Backfill stage must match configured STAGE" } }
        val run = Run(stage, partyId.value.toString(), runId)
        val ledger = loadLedger(run)
        val existing = loadCheckpoint(run)
        check(existing == null || ledger["phase"] == "BACKFILLING") { "Existing checkpoint requires its active backfill ledger" }
        var checkpoint = existing ?: initialize(run, ledger)
        while (!checkpoint.complete) checkpoint = importPage(run, ledger, checkpoint)
        return PartyConfigurationBackfillResult(checkpoint.count, checkpoint.lastKey, checkpoint.complete)
    }

    private suspend fun loadLedger(run: Run): Json {
        val ledger = get(coreTable, run.partition, "STATE") ?: error("Migration ledger is required")
        check(ledger["targetSchemaVersion"] == TARGET_SCHEMA_VERSION) { "Unsupported target schema version for ${run.stage}" }
        val phase = ledger["phase"]
        check(phase == "DISABLED" || phase == "BACKFILLING") { "Backfill requires disabled or backfilling phase" }
        check(phase == "DISABLED" || ledger["activeRunId"] == run.id) { "Migration run is owned by another worker" }
        check(ledger["ledgerVersion"] is Number) { "Migration ledger version is required" }
        return ledger
    }

    private suspend fun loadCheckpoint(run: Run): Checkpoint? = get(coreTable, run.partition, run.checkpointKey)?.let {
        check(it["runId"] == run.id && it["targetSchemaVersion"] == TARGET_SCHEMA_VERSION) { "Backfill checkpoint belongs to another run or schema" }
        check(it["source"] == sourceTable && it["partyId"] == run.partyId) { "Backfill source identity changed" }
        Checkpoint(
            watermark = it.getDynamoStringValue("sourceWatermark"),
            lastKey = it.getDynamoStringValue("lastKey"),
            count = it.getDynamoNumberValue("completedCount")!!.toInt(),
            checksum = it.getDynamoStringValue("checksum")!!,
            complete = it.getDynamoBoolValue("complete")!!,
            metadataPresent = it.getDynamoBoolValue("metadataPresent")!!,
        )
    }

    private suspend fun initialize(run: Run, ledger: Json): Checkpoint {
        check(get(coreTable, "PARTY#${run.partyId}", "META") == null) { "Target party metadata already exists" }
        check(queryHistory(run).isEmpty()) { "Target party history already exists" }
        val watermark = querySource(run, descending = true).items.firstOrNull()?.getDynamoStringValue("timestamp")
        val checkpoint = Checkpoint(watermark, null, 0, sha256(""), watermark == null, false)
        val items = arrayOf(startLedger(run, ledger), checkpointPut(run, checkpoint, null))
        dynamoDBClient.transactWrite(TransactWriteCommandInput(transactItems = items)).await()
        if (ledger["phase"] == "DISABLED") {
            ledger["phase"] = "BACKFILLING"
            ledger["activeRunId"] = run.id
            ledger["ledgerVersion"] = ledger.getDynamoNumberValue("ledgerVersion")!!.toInt() + 1
        }
        return checkpoint
    }

    private fun startLedger(run: Run, ledger: Json): TransactWriteItem = if (ledger["phase"] == "DISABLED") {
        val next = json().add(ledger).add(
            json(
                "phase" to "BACKFILLING",
                "activeRunId" to run.id,
                "ledgerVersion" to ledger.getDynamoNumberValue("ledgerVersion")!!.toInt() + 1,
                "updatedAt" to Clock.System.now().toString(),
            ),
        )
        TransactWriteItem(
            put = DynamoPutItemInput(
                tableName = coreTable,
                item = next,
                conditionExpression = "targetSchemaVersion = :schema AND ledgerVersion = :version AND phase = :phase",
                expressionAttributeValues = json(":schema" to TARGET_SCHEMA_VERSION, ":version" to ledger["ledgerVersion"], ":phase" to "DISABLED"),
            ),
        )
    } else {
        ledgerGuard(run, ledger)
    }

    private suspend fun importPage(run: Run, ledger: Json, start: Checkpoint): Checkpoint {
        val page = querySource(run, checkpoint = start)
        check(page.items.isNotEmpty()) { "Legacy snapshot changed before its watermark was imported" }
        var checkpoint = start
        for (source in page.items) checkpoint = importRevision(run, ledger, checkpoint, source)
        return checkpoint
    }

    private suspend fun importRevision(run: Run, ledger: Json, previous: Checkpoint, source: Json): Checkpoint {
        val timestamp = source.getDynamoDateTimeValue("timestamp") ?: error("Source timestamp is required")
        check(timestamp.isoWithMillis() == source["timestamp"]) { "Source timestamp must use canonical millisecond precision" }
        val revisionId = backfillRevisionId(timestamp, JSON.stringify(arrayOf(sourceTable, run.partyId, source["timestamp"])))
        val history = historyItem(run, previous, source, revisionId)
        val next = Checkpoint(
            previous.watermark,
            source.getDynamoStringValue("timestamp"),
            previous.count + 1,
            sha256("${previous.checksum}\n${JSON.stringify(history)}"),
            source["timestamp"] == previous.watermark,
            source.getDynamoBoolValue("isDeleted") != true,
        )
        val items = arrayOf(
            ledgerGuard(run, ledger),
            TransactWriteItem(put = DynamoPutItemInput(tableName = historyTable, item = history, conditionExpression = "attribute_not_exists(PK)")),
            metadataWrite(run, previous, next, source),
            checkpointPut(run, next, previous),
        )
        dynamoDBClient.transactWrite(TransactWriteCommandInput(transactItems = items)).await()
        return next
    }

    private fun historyItem(run: Run, previous: Checkpoint, source: Json, revisionId: String) = json(
        "PK" to "ENTITY#PARTY#PARTY#${run.partyId}",
        "SK" to "REV#$revisionId",
        "actorId" to source.getDynamoStringValue("modifyingUserEmail"),
        "action" to if (source.getDynamoBoolValue("isDeleted") == true) "DELETE_PARTY" else "SAVE_PARTY",
        "timestamp" to source["timestamp"],
        "version" to previous.count + 1,
        "party" to canonicalParty(source),
        "isDeleted" to (source.getDynamoBoolValue("isDeleted") == true),
        "source" to sourceTable,
        "sourceId" to run.partyId,
        "sourceTimestamp" to source["timestamp"],
    )

    private fun canonicalParty(source: Json): Json = source.toParty().let { party ->
        party.asDynamoJson().add(json("imageURL" to party.imageURL))
    }

    private fun metadataWrite(run: Run, previous: Checkpoint, next: Checkpoint, source: Json): TransactWriteItem {
        val condition = if (previous.metadataPresent) "version = :version AND backfillRunId = :run" else "attribute_not_exists(PK)"
        val values = if (previous.metadataPresent) json(":version" to previous.count, ":run" to run.id) else null
        return if (next.metadataPresent) {
            TransactWriteItem(
                put = DynamoPutItemInput(
                    tableName = coreTable,
                    item = canonicalParty(source).add(
                        json(
                            "PK" to "PARTY#${run.partyId}",
                            "SK" to "META",
                            "version" to next.count,
                            "modifyingUserId" to source.getDynamoStringValue("modifyingUserEmail"),
                            "timestamp" to source["timestamp"],
                            "backfillRunId" to run.id,
                        ),
                    ),
                    conditionExpression = condition,
                    expressionAttributeValues = values.dynamoOptional(),
                ),
            )
        } else {
            TransactWriteItem(
                delete = DynamoDeleteItemInput(
                    tableName = coreTable,
                    key = json("PK" to "PARTY#${run.partyId}", "SK" to "META"),
                    conditionExpression = condition,
                    expressionAttributeValues = values.dynamoOptional(),
                ),
            )
        }
    }

    private fun checkpointPut(run: Run, next: Checkpoint, previous: Checkpoint?) = TransactWriteItem(
        put = DynamoPutItemInput(
            tableName = coreTable,
            item = json(
                "PK" to run.partition, "SK" to run.checkpointKey,
                "targetSchemaVersion" to TARGET_SCHEMA_VERSION, "runId" to run.id, "source" to sourceTable, "partyId" to run.partyId,
                "sourceWatermark" to next.watermark, "lastKey" to next.lastKey, "completedCount" to next.count,
                "checksum" to next.checksum, "complete" to next.complete, "metadataPresent" to next.metadataPresent,
            ),
            conditionExpression = if (previous == null) {
                "attribute_not_exists(PK)"
            } else {
                "runId = :run AND completedCount = :count AND checksum = :checksum AND complete = :complete"
            },
            expressionAttributeValues = previous?.let {
                json(":run" to run.id, ":count" to it.count, ":checksum" to it.checksum, ":complete" to false)
            }.dynamoOptional(),
        ),
    )

    private fun ledgerGuard(run: Run, ledger: Json) = TransactWriteItem(
        conditionCheck = DynamoConditionCheck(
            tableName = coreTable,
            key = DynamoKey(partitionKey = run.partition, sortKey = "STATE"),
            conditionExpression = "targetSchemaVersion = :schema AND ledgerVersion = :version AND phase = :phase AND activeRunId = :run",
            expressionAttributeValues = json(
                ":schema" to TARGET_SCHEMA_VERSION,
                ":version" to ledger["ledgerVersion"],
                ":phase" to "BACKFILLING",
                ":run" to run.id,
            ),
        ),
    )

    private suspend fun querySource(run: Run, descending: Boolean = false, checkpoint: Checkpoint? = null) = dynamoDBClient.queryItems(
        DynamoQueryInput(
            tableName = sourceTable,
            keyConditionExpression = if (checkpoint == null) "#id = :id" else "#id = :id AND #timestamp <= :watermark",
            expressionAttributeNames = if (checkpoint == null) json("#id" to "id") else json("#id" to "id", "#timestamp" to "timestamp"),
            expressionAttributeValues = if (checkpoint == null) json(":id" to run.partyId) else json(":id" to run.partyId, ":watermark" to checkpoint.watermark),
            consistentRead = true,
            scanIndexForward = !descending,
            limit = if (descending) 1 else pageSize,
            exclusiveStartKey = checkpoint?.lastKey?.let { json("id" to run.partyId, "timestamp" to it) }.dynamoOptional(),
        ),
    ).await()

    private suspend fun queryHistory(run: Run) = dynamoDBClient.queryItems(
        DynamoQueryInput(
            tableName = historyTable,
            keyConditionExpression = "#pk = :pk",
            expressionAttributeNames = json("#pk" to "PK"),
            expressionAttributeValues = json(":pk" to "ENTITY#PARTY#PARTY#${run.partyId}"),
            consistentRead = true,
            scanIndexForward = true,
            limit = 1,
        ),
    ).await().items

    private suspend fun get(table: String, pk: String, sk: String) = dynamoDBClient.getItem(
        GetItemCommandInput(tableName = table, key = DynamoKey(partitionKey = pk, sortKey = sk), consistentRead = true),
    ).await().item

    private val coreTable get() = targetTableName("coupling-core")
    private val historyTable get() = targetTableName("coupling-history")
    private val sourceTable get() = "${legacyDynamoPrefix()}TRIBE"

    private data class Run(val stage: String, val partyId: String, val id: String) {
        val partition get() = "MIGRATION#$stage"
        val checkpointKey get() = "BACKFILL#TRIBE#$partyId"
    }

    private data class Checkpoint(
        val watermark: String?,
        val lastKey: String?,
        val count: Int,
        val checksum: String,
        val complete: Boolean,
        val metadataPresent: Boolean,
    )
}
