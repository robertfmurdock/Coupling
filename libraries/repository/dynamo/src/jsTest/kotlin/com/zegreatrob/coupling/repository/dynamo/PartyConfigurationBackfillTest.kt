package com.zegreatrob.coupling.repository.dynamo

import com.zegreatrob.coupling.model.party.defaultParty
import com.zegreatrob.coupling.repository.dynamo.external.awslibdynamodb.DynamoDBDocumentClient
import com.zegreatrob.coupling.repository.dynamo.external.awslibdynamodb.TransactWriteCommand
import com.zegreatrob.coupling.repository.dynamo.external.awslibdynamodb.TransactionCanceledError
import com.zegreatrob.coupling.repository.dynamo.external.get
import com.zegreatrob.coupling.repository.dynamo.external.put
import com.zegreatrob.coupling.repository.dynamo.external.query
import com.zegreatrob.coupling.repository.party.PartyConfigurationBackfillResult
import com.zegreatrob.coupling.repository.validation.MagicClock
import com.zegreatrob.coupling.stubmodel.stubPartyDetails
import com.zegreatrob.coupling.stubmodel.stubUserDetails
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.asyncSetup
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.await
import kotlinx.coroutines.promise
import kotlin.js.Json
import kotlin.js.json
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.time.Instant

class PartyConfigurationBackfillTest {

    @Test
    fun importsAllPagesAndReplaysWithoutDuplicatingHistory() = asyncSetup(Fixture()) {
        prepare()
        source("20240101000000.000", "Original")
        source("20240102000000.000", "Updated")
        source("20240103000000.000", "Current")
        source("20240101000000.000", "Integration", id = "INTEGRATION-${party.id.value}")
    } exercise {
        val sourceBefore = JSON.stringify(legacy().toTypedArray())
        val first = DynamoPartyConfigurationBackfill(pageSize = 1).backfill(stage, party.id, runId)
        val second = DynamoPartyConfigurationBackfill(pageSize = 1).backfill(stage, party.id, runId)
        SnapshotImportResult(first, second, history(), sourceBefore)
    } verify { (first, second, revisions, sourceBefore) ->
        first.importedCount.assertIsEqualTo(3)
        first.checkpoint.assertIsEqualTo("20240103000000.000")
        first.complete.assertIsEqualTo(true)
        second.assertIsEqualTo(first)
        revisions.map { it["version"] }.assertIsEqualTo(listOf(1, 2, 3))
        revisions.map { it["actorId"] }.assertIsEqualTo(List(3) { actor.id.value.toString() })
        revisions.map { it["timestamp"] }.assertIsEqualTo(listOf("20240101000000.000", "20240102000000.000", "20240103000000.000"))
        revisions.map { it["party"].unsafeCast<Json>()["name"] }.assertIsEqualTo(listOf("Original", "Updated", "Current"))
        revisions.map { it["SK"].toString() }.let { it.assertIsEqualTo(it.sorted()) }
        val current = TargetPartyConfigurationRepository(actor.id, MagicClock()).getDetails(party.id)
        current?.data.assertIsEqualTo(defaultParty.copy(id = party.id, name = "Current", defaultBadgeName = "", alternateBadgeName = ""))
        current?.timestamp.assertIsEqualTo(Instant.parse("2024-01-03T00:00:00Z"))
        current?.modifyingUserId.assertIsEqualTo(actor.id.value)
        ledger()?.get("phase").assertIsEqualTo("BACKFILLING")
        JSON.stringify(legacy().toTypedArray()).assertIsEqualTo(sourceBefore)
    }

    @Test
    fun interruptionResumesFromTheLastAtomicCheckpointAndExcludesLaterWrites() = asyncSetup(Fixture()) {
        prepare()
        source("20240101000000.000", "First")
        source("20240102000000.000", "Snapshot")
    } exercise {
        var imports = 0
        val syntax = interceptTransactions { command ->
            if (command.input.transactItems.any { it.put?.tableName == "test-coupling-history" }) {
                imports++
                if (imports == 2) error("interrupted")
            }
        }
        assertFailsWith<IllegalStateException> {
            DynamoPartyConfigurationBackfill(syntax, pageSize = 1).backfill(stage, party.id, runId)
        }
        val interruptedCheckpoint = checkpoint()
        val interruptedHistory = history()
        val interruptedMetadata = metadata()
        source("20240103000000.000", "Outside snapshot")
        val result = DynamoPartyConfigurationBackfill(pageSize = 1).backfill(stage, party.id, runId)
        ResumeResult(interruptedCheckpoint, interruptedHistory, interruptedMetadata, result)
    } verify { result ->
        result.checkpoint?.get("completedCount").assertIsEqualTo(1)
        result.history.size.assertIsEqualTo(1)
        result.metadata?.get("name").assertIsEqualTo("First")
        val completed = result.result
        completed.importedCount.assertIsEqualTo(2)
        history().size.assertIsEqualTo(2)
        metadata()?.get("name").assertIsEqualTo("Snapshot")
    }

    @Test
    fun deletedLatestRevisionLeavesNoCurrentMetadata() = asyncSetup(Fixture()) {
        prepare()
        source("20240101000000.000", "First")
        source("20240102000000.000", "First", deleted = true)
    } exercise {
        DynamoPartyConfigurationBackfill().backfill(stage, party.id, runId)
    } verify {
        metadata().assertIsEqualTo(null)
        history().map { it["action"] }.assertIsEqualTo(listOf("SAVE_PARTY", "DELETE_PARTY"))
        history().map { it["isDeleted"] }.assertIsEqualTo(listOf(false, true))
    }

    @Test
    fun restorationAfterDeletionRecreatesCurrentMetadata() = asyncSetup(Fixture()) {
        prepare()
        source("20240101000000.000", "First")
        source("20240102000000.000", "First", deleted = true)
        source("20240103000000.000", "Restored")
    } exercise {
        DynamoPartyConfigurationBackfill().backfill(stage, party.id, runId)
    } verify {
        metadata()?.get("name").assertIsEqualTo("Restored")
        metadata()?.get("version").assertIsEqualTo(3)
        history().size.assertIsEqualTo(3)
    }

    @Test
    fun absentPartyCompletesAnEmptySnapshot() = asyncSetup(Fixture()) {
        prepare()
    } exercise {
        DynamoPartyConfigurationBackfill().backfill(stage, party.id, runId)
    } verify { result ->
        result.importedCount.assertIsEqualTo(0)
        result.checkpoint.assertIsEqualTo(null)
        result.complete.assertIsEqualTo(true)
        history().assertIsEqualTo(emptyList())
        checkpoint()?.get("complete").assertIsEqualTo(true)
    }

    @Test
    fun existingTargetMetadataFailsWithoutHistoryOrCheckpointAdvancement() = asyncSetup(Fixture()) {
        prepare()
        source("20240101000000.000", "Source")
        TargetPartyConfigurationRepository(actor.id, MagicClock()).save(party)
    } exercise {
        val failure = try {
            DynamoPartyConfigurationBackfill().backfill(stage, party.id, runId)
            null
        } catch (error: Throwable) {
            error
        }
        failure
    } verify { failure ->
        (failure != null).assertIsEqualTo(true)
        metadata()?.get("name").assertIsEqualTo(party.name)
        history().size.assertIsEqualTo(1)
        checkpoint().assertIsEqualTo(null)
    }

    @Test
    fun unsupportedLedgerIsPreservedAndNoImportStarts() = asyncSetup(Fixture()) {
        prepare()
        source("20240101000000.000", "Source")
        val state = ledger()!!
        state["targetSchemaVersion"] = 2
        put("test-coupling-core", state)
    } exercise {
        assertFailsWith<IllegalStateException> {
            DynamoPartyConfigurationBackfill().backfill(stage, party.id, runId)
        }
    } verify {
        ledger()?.get("targetSchemaVersion").assertIsEqualTo(2)
        checkpoint().assertIsEqualTo(null)
        history().assertIsEqualTo(emptyList())
    }

    @Test
    fun competingWorkerCannotAdvanceAStaleCheckpoint() = asyncSetup(Fixture()) {
        prepare()
        source("20240101000000.000", "First")
        source("20240102000000.000", "Second")
        source("20240103000000.000", "Third")
    } exercise {
        var imports = 0
        val syntax = interceptTransactions { transaction ->
            if (transaction.input.transactItems.any { it.put?.tableName == "test-coupling-history" }) {
                imports++
                if (imports == 2) DynamoPartyConfigurationBackfill(pageSize = 1).backfill(stage, party.id, runId)
            }
        }
        val failure = try {
            DynamoPartyConfigurationBackfill(syntax, pageSize = 1).backfill(stage, party.id, runId)
            null
        } catch (error: Throwable) {
            error
        }
        failure to DynamoPartyConfigurationBackfill().backfill(stage, party.id, runId)
    } verify { (failure, replay) ->
        failure?.unsafeCast<TransactionCanceledError>()?.name.assertIsEqualTo("TransactionCanceledException")
        replay.importedCount.assertIsEqualTo(3)
        replay.complete.assertIsEqualTo(true)
        history().map { it["version"] }.assertIsEqualTo(listOf(1, 2, 3))
        metadata()?.get("name").assertIsEqualTo("Third")
        checkpoint()?.get("completedCount").assertIsEqualTo(3)
    }

    @Test
    fun ledgerChangeDuringImportPreventsAllRevisionEffects() = asyncSetup(Fixture()) {
        prepare()
        source("20240101000000.000", "Source")
    } exercise {
        val syntax = interceptTransactions { transaction ->
            if (transaction.input.transactItems.any { it.put?.tableName == "test-coupling-history" }) {
                val state = ledger()!!
                state["phase"] = "CUTOVER"
                state["ledgerVersion"] = state["ledgerVersion"].unsafeCast<Number>().toInt() + 1
                put("test-coupling-core", state)
            }
        }
        try {
            DynamoPartyConfigurationBackfill(syntax).backfill(stage, party.id, runId)
            null
        } catch (error: Throwable) {
            error
        }
    } verify { failure ->
        failure?.unsafeCast<TransactionCanceledError>()?.name.assertIsEqualTo("TransactionCanceledException")
        checkpoint()?.get("completedCount").assertIsEqualTo(0)
        history().assertIsEqualTo(emptyList())
        metadata().assertIsEqualTo(null)
        ledger()?.get("phase").assertIsEqualTo("CUTOVER")
    }

    @Test
    fun missingLedgerSchemaIsRejectedBeforeAnyWrites() = asyncSetup(Fixture()) {
        prepare()
        put("test-coupling-core", json("PK" to "MIGRATION#$stage", "SK" to "STATE", "phase" to "DISABLED", "ledgerVersion" to 1))
    } exercise {
        assertFailsWith<IllegalStateException> {
            DynamoPartyConfigurationBackfill().backfill(stage, party.id, runId)
        }
    } verify {
        ledger()?.get("phase").assertIsEqualTo("DISABLED")
        checkpoint().assertIsEqualTo(null)
    }

    @Test
    fun dualWriteAndCutoverLedgersRejectBackfill() = asyncSetup(object {
        val dualWrite = Fixture()
        val cutover = Fixture()
    }) {
        dualWrite.prepare()
        cutover.prepare()
        dualWrite.changePhase("DUAL_WRITE")
        cutover.changePhase("CUTOVER")
    } exercise {
        val repository = DynamoPartyConfigurationBackfill()
        assertFailsWith<IllegalStateException> { repository.backfill(dualWrite.stage, dualWrite.party.id, dualWrite.runId) }
        assertFailsWith<IllegalStateException> { repository.backfill(cutover.stage, cutover.party.id, cutover.runId) }
    } verify {
        dualWrite.ledger()?.get("phase").assertIsEqualTo("DUAL_WRITE")
        cutover.ledger()?.get("phase").assertIsEqualTo("CUTOVER")
        dualWrite.checkpoint().assertIsEqualTo(null)
        cutover.checkpoint().assertIsEqualTo(null)
    }

    @Test
    fun differentRunCannotTakeOverAnExistingCheckpoint() = asyncSetup(Fixture()) {
        prepare()
        source("20240101000000.000", "Source")
        DynamoPartyConfigurationBackfill().backfill(stage, party.id, runId)
    } exercise {
        assertFailsWith<IllegalStateException> {
            DynamoPartyConfigurationBackfill().backfill(stage, party.id, "different-run")
        }
    } verify {
        checkpoint()?.get("runId").assertIsEqualTo(runId)
        history().size.assertIsEqualTo(1)
    }

    @Test
    fun lostTransactionResponseReplaysTheDurableOutcome() = asyncSetup(Fixture()) {
        prepare()
        source("20240101000000.000", "Source")
    } exercise {
        val syntax = interceptTransactions(after = { transaction ->
            if (transaction.input.transactItems.any { it.put?.tableName == "test-coupling-history" }) error("response lost")
        }) { }
        assertFailsWith<IllegalStateException> {
            DynamoPartyConfigurationBackfill(syntax).backfill(stage, party.id, runId)
        }
        val committedCheckpoint = JSON.stringify(checkpoint())
        val replay = DynamoPartyConfigurationBackfill().backfill(stage, party.id, runId)
        replay to committedCheckpoint
    } verify { (replay, committedCheckpoint) ->
        replay.importedCount.assertIsEqualTo(1)
        replay.complete.assertIsEqualTo(true)
        history().size.assertIsEqualTo(1)
        metadata()?.get("version").assertIsEqualTo(1)
        JSON.stringify(checkpoint()).assertIsEqualTo(committedCheckpoint)
        Regex("[0-9a-f]{64}").matches(checkpoint()!!["checksum"].toString()).assertIsEqualTo(true)
    }

    @Test
    fun historyCollisionDuringImportDoesNotAdvanceMetadataOrCheckpoint() = asyncSetup(Fixture()) {
        prepare()
        source("20240101000000.000", "Source")
    } exercise {
        val syntax = interceptTransactions { transaction ->
            transaction.input.transactItems.mapNotNull { it.put }.find { it.tableName == "test-coupling-history" }?.let {
                put("test-coupling-history", json("PK" to it.item["PK"], "SK" to it.item["SK"], "marker" to "preexisting"))
            }
        }
        try {
            DynamoPartyConfigurationBackfill(syntax).backfill(stage, party.id, runId)
            null
        } catch (error: Throwable) {
            error
        }
    } verify { failure ->
        failure?.unsafeCast<TransactionCanceledError>()?.name.assertIsEqualTo("TransactionCanceledException")
        metadata().assertIsEqualTo(null)
        checkpoint()?.get("completedCount").assertIsEqualTo(0)
        history().single()["marker"].assertIsEqualTo("preexisting")
    }

    @Test
    fun disabledLedgerCannotResumeAnExistingCheckpoint() = asyncSetup(Fixture()) {
        prepare()
        DynamoPartyConfigurationBackfill().backfill(stage, party.id, runId)
        changePhase("DISABLED")
    } exercise {
        assertFailsWith<IllegalStateException> {
            DynamoPartyConfigurationBackfill().backfill(stage, party.id, runId)
        }
    } verify {
        ledger()?.get("phase").assertIsEqualTo("DISABLED")
        checkpoint()?.get("complete").assertIsEqualTo(true)
    }
}

private class Fixture {
    val party = stubPartyDetails()
    val actor = stubUserDetails()
    val stage = "party-backfill-${party.id.value}"
    val runId = "run-${party.id.value}"

    suspend fun prepare() {
        TargetSchemaBootstrap.ensureUncached(stage, MigrationMode.DISABLED)
        DynamoPartyRepository(actor.id, MagicClock())
    }

    suspend fun source(timestamp: String, name: String, deleted: Boolean = false, id: String = party.id.value.toString()) = put(
        "test-TRIBE",
        json("id" to id, "timestamp" to timestamp, "name" to name, "modifyingUserEmail" to actor.id.value.toString(), "isDeleted" to deleted),
    )

    suspend fun metadata() = get("PARTY#${party.id.value}", "META")
    suspend fun ledger() = get("MIGRATION#$stage", "STATE")
    suspend fun checkpoint() = get("MIGRATION#$stage", "BACKFILL#TRIBE#${party.id.value}")
    suspend fun history() = query("test-coupling-history", "PK", "ENTITY#PARTY#PARTY#${party.id.value}")
    suspend fun legacy() = query("test-TRIBE", "id", party.id.value.toString())

    private suspend fun get(pk: String, sk: String) = DynamoDbProvider.dynamoDBClient.get(
        json("TableName" to "test-coupling-core", "Key" to json("PK" to pk, "SK" to sk), "ConsistentRead" to true),
    ).await()["Item"].unsafeCast<Json?>()

    private suspend fun query(table: String, key: String, value: String) = DynamoDbProvider.dynamoDBClient.query(
        json("TableName" to table, "KeyConditionExpression" to "#key = :key", "ExpressionAttributeNames" to json("#key" to key), "ExpressionAttributeValues" to json(":key" to value), "ConsistentRead" to true),
    ).await()["Items"].unsafeCast<Array<Json>>().toList()

    suspend fun changePhase(phase: String) {
        val state = ledger()!!
        state["phase"] = phase
        put("test-coupling-core", state)
    }

    suspend fun put(table: String, item: Json) {
        DynamoDbProvider.dynamoDBClient.put(json("TableName" to table, "Item" to item)).await()
    }
}

private fun interceptTransactions(
    after: suspend (TransactWriteCommand) -> Unit = {},
    before: suspend (TransactWriteCommand) -> Unit,
) = object : DynamoDBSyntax {
    override val dynamoDB = DynamoDbProvider.dynamoDB
    override val dynamoDBClient = js("({})").unsafeCast<DynamoDBDocumentClient>().also { client ->
        client.asDynamic().send = { command: Any ->
            MainScope().promise {
                if (command is TransactWriteCommand) before(command)
                val result = DynamoDbProvider.dynamoDBClient.send<Json>(command).await()
                if (command is TransactWriteCommand) after(command)
                result
            }
        }
    }
}

private data class ResumeResult(
    val checkpoint: Json?,
    val history: List<Json>,
    val metadata: Json?,
    val result: PartyConfigurationBackfillResult,
)

private data class SnapshotImportResult(
    val first: PartyConfigurationBackfillResult,
    val replay: PartyConfigurationBackfillResult,
    val history: List<Json>,
    val sourceBefore: String,
)
