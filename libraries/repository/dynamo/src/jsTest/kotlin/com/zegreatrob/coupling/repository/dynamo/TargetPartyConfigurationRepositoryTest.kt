package com.zegreatrob.coupling.repository.dynamo

import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.party.PairingRule
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.repository.dynamo.external.awsdynamoclient.DynamoDB
import com.zegreatrob.coupling.repository.dynamo.external.awslibdynamodb.DynamoDBDocumentClient
import com.zegreatrob.coupling.repository.dynamo.external.awslibdynamodb.GetItemCommandInput
import com.zegreatrob.coupling.repository.dynamo.external.awslibdynamodb.TransactWriteCommand
import com.zegreatrob.coupling.repository.dynamo.external.awslibdynamodb.TransactWriteCommandInput
import com.zegreatrob.coupling.repository.dynamo.external.awslibdynamodb.TransactionCanceledError
import com.zegreatrob.coupling.repository.dynamo.external.awslibdynamodb.TypedGetCommand
import com.zegreatrob.coupling.repository.dynamo.external.get
import com.zegreatrob.coupling.repository.dynamo.external.put
import com.zegreatrob.coupling.repository.dynamo.external.query
import com.zegreatrob.coupling.repository.validation.MagicClock
import com.zegreatrob.coupling.stubmodel.stubPartyDetails
import com.zegreatrob.coupling.stubmodel.stubUserDetails
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.asyncSetup
import com.zegreatrob.testmints.setup
import kotlinx.coroutines.await
import kotlin.js.Json
import kotlin.js.Promise
import kotlin.js.json
import kotlin.test.Test
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days

class TargetPartyConfigurationRepositoryTransportTest {

    @Test
    fun retriesOnlyTheCoreVersionConflictWithAConsistentDirectRead() = asyncSetup(object {
        val user = stubUserDetails()
        val party = stubPartyDetails()
        val updatedParty = party.copy(name = "Updated party")
        val commands = mutableListOf<RecordedDynamoCommand>()
        val outcomes = mutableListOf(
            Promise.resolve(json("Item" to metadata(party, user.id.value.toString(), 1))),
            Promise.reject(transactionCanceledAtCoreVersion()),
            Promise.resolve(json("Item" to metadata(party, user.id.value.toString(), 2))),
            Promise.resolve(json()),
        )
        lateinit var repository: TargetPartyConfigurationRepository
    }) {
        repository = TargetPartyConfigurationRepository(
            user.id,
            MagicClock(),
            { "01J0000000000000000000000${commands.size}" },
            recordingDynamoSyntax(commands, outcomes),
        )
    } exercise {
        repository.save(updatedParty)
        commands
    } verify { commands ->
        val reads = commands.filterIsInstance<RecordedGet>()
        reads.map { it.input.consistentRead }.assertIsEqualTo(listOf(true, true))
        reads.map { it.input.key.partitionKey }
            .assertIsEqualTo(listOf("PARTY#${party.id.value}", "PARTY#${party.id.value}"))
        commands.filterIsInstance<RecordedTransaction>()
            .map { it.currentMetadataWrite.version }
            .assertIsEqualTo(listOf(2, 3))
    }

    @Test
    fun doesNotRetryAHistoryRevisionCollision() = asyncSetup(object {
        val user = stubUserDetails()
        val party = stubPartyDetails()
        val commands = mutableListOf<RecordedDynamoCommand>()
        val outcomes = mutableListOf(
            Promise.resolve(json("Item" to metadata(party, user.id.value.toString(), 1))),
            Promise.reject(transactionCanceledAtHistoryRevision()),
        )
        lateinit var repository: TargetPartyConfigurationRepository
    }) {
        repository = TargetPartyConfigurationRepository(
            user.id,
            MagicClock(),
            { "01J00000000000000000000000" },
            recordingDynamoSyntax(commands, outcomes),
        )
    } exercise {
        val failure = try {
            repository.save(party)
            null
        } catch (error: Throwable) {
            error
        }
        TargetPartyTransportFailureResult(commands, failure)
    } verify { result: TargetPartyTransportFailureResult ->
        result.failure?.name().assertIsEqualTo("TransactionCanceledException")
        result.commands.filterIsInstance<RecordedGet>().size.assertIsEqualTo(1)
        result.commands.filterIsInstance<RecordedTransaction>().size.assertIsEqualTo(1)
    }
}

class MonotonicUlidRevisionIdGeneratorTest {

    @Test
    fun createsOrderedRevisionIdsForTheSameTimestamp() = setup(object {
        val timestamp = Clock.System.now()
        val generator = MonotonicUlidRevisionIdGenerator()
    }) exercise {
        listOf(generator.next(timestamp), generator.next(timestamp))
    } verify { revisionIds ->
        revisionIds.assertIsEqualTo(revisionIds.sorted())
    }
}

class TargetPartyConfigurationRepositoryTest {

    @Test
    fun returnsNoPartyConfigurationWhenMetadataDoesNotExist() = asyncSetup(object {
        val stage = "target-party-configuration-missing"
        val user = stubUserDetails()
        val party = stubPartyDetails().copy(imageURL = "https://example.test/party.png")
        lateinit var repository: TargetPartyConfigurationRepository
    }) {
        TargetSchemaBootstrap.ensureUncached(stage, MigrationMode.DISABLED)
        repository = TargetPartyConfigurationRepository(user.id, MagicClock())
    } exercise {
        repository.getDetails(party.id)
    } verify { result ->
        result.assertIsEqualTo(null)
    }

    @Test
    fun savesThenLoadsPartyConfigurationFromCoreMetadata() = asyncSetup(object {
        val stage = "target-party-configuration"
        val user = stubUserDetails()
        val party = stubPartyDetails().copy(imageURL = "https://example.test/party.png")
        val clock = MagicClock().apply { currentTime = Clock.System.now().minus(3.days) }
        lateinit var repository: TargetPartyConfigurationRepository
    }) {
        TargetSchemaBootstrap.ensureUncached(stage, MigrationMode.DISABLED)
        repository = TargetPartyConfigurationRepository(user.id, clock, dynamoDBSyntax = DynamoDbProvider)
    } exercise {
        repository.save(party)
        TargetPartyConfigurationResult(
            repository.getDetails(party.id),
            coreMetadata(party.id.value.toString()),
        )
    } verify { result: TargetPartyConfigurationResult ->
        result.party?.data.assertIsEqualTo(party)
        result.party?.modifyingUserId.assertIsEqualTo(user.id.value)
        result.party?.timestamp.assertIsEqualTo(clock.currentTime)
        result.metadata["PK"].assertIsEqualTo("PARTY#${party.id.value}")
        result.metadata["SK"].assertIsEqualTo("META")
        result.metadata["version"].assertIsEqualTo(1)
    }

    @Test
    fun overwritingPartyConfigurationAdvancesMetadataAndPreservesBothRevisions() = asyncSetup(object {
        val stage = "target-party-configuration-history"
        val user = stubUserDetails()
        val party = stubPartyDetails().copy(imageURL = "https://example.test/party.png")
        val updatedParty = party.copy(name = "Updated party", imageURL = "https://example.test/updated-party.png")
        val clock = MagicClock().apply { currentTime = Clock.System.now().minus(3.days) }
        val revisionIds = listOf("01J00000000000000000000000", "01J00000000000000000000001").iterator()
        lateinit var repository: TargetPartyConfigurationRepository
    }) {
        TargetSchemaBootstrap.ensureUncached(stage, MigrationMode.DISABLED)
        repository = TargetPartyConfigurationRepository(
            user.id,
            clock,
            { revisionIds.next() },
        )
    } exercise {
        repository.save(party)
        clock.currentTime = Clock.System.now().minus(2.days)
        repository.save(updatedParty)
        TargetPartyHistoryResult(repository.getDetails(party.id), history(party.id.value.toString()))
    } verify { result: TargetPartyHistoryResult ->
        result.current?.data.assertIsEqualTo(updatedParty)
        result.revisions.map { it["SK"] }
            .assertIsEqualTo(listOf("REV#01J00000000000000000000000", "REV#01J00000000000000000000001"))
        result.revisions.map { it["version"] }.assertIsEqualTo(listOf(1, 2))
        result.revisions.map { it["action"] }.assertIsEqualTo(listOf("SAVE_PARTY", "SAVE_PARTY"))
        result.revisions.map { it["actorId"] }.assertIsEqualTo(listOf(user.id.value.toString(), user.id.value.toString()))
        result.revisions.map { it["party"].unsafeCast<Json>()["name"] }
            .assertIsEqualTo(listOf(party.name, updatedParty.name))
        result.revisions.map { it["party"].unsafeCast<Json>()["imageURL"] }
            .assertIsEqualTo(listOf(party.imageURL, updatedParty.imageURL))
    }

    @Test
    fun historyRevisionCollisionDoesNotCommitCoreMetadata() = asyncSetup(object {
        val stage = "target-party-configuration-atomic-history"
        val user = stubUserDetails()
        val party = stubPartyDetails()
        val revisionId = "01J00000000000000000000000"
        lateinit var repository: TargetPartyConfigurationRepository
    }) {
        TargetSchemaBootstrap.ensureUncached(stage, MigrationMode.DISABLED)
        DynamoDbProvider.dynamoDBClient.put(
            json(
                "TableName" to "test-coupling-history",
                "Item" to json(
                    "PK" to "ENTITY#PARTY#PARTY#${party.id.value}",
                    "SK" to "REV#$revisionId",
                ),
            ),
        ).await()
        repository = TargetPartyConfigurationRepository(
            user.id,
            MagicClock(),
            { revisionId },
        )
    } exercise {
        val failure = try {
            repository.save(party)
            null
        } catch (error: Throwable) {
            error
        }
        TargetPartyAtomicFailureResult(failure, coreMetadataOrNull(party.id.value.toString()))
    } verify { result: TargetPartyAtomicFailureResult ->
        result.failure?.name().assertIsEqualTo("TransactionCanceledException")
        result.metadata.assertIsEqualTo(null)
    }
}

private suspend fun coreMetadata(partyId: String) = DynamoDbProvider.dynamoDBClient.get(
    json(
        "TableName" to "test-coupling-core",
        "Key" to json("PK" to "PARTY#$partyId", "SK" to "META"),
    ),
).await()["Item"].unsafeCast<Json>()

private suspend fun coreMetadataOrNull(partyId: String) = DynamoDbProvider.dynamoDBClient.get(
    json(
        "TableName" to "test-coupling-core",
        "Key" to json("PK" to "PARTY#$partyId", "SK" to "META"),
    ),
).await()["Item"].unsafeCast<Json?>()

private suspend fun history(partyId: String) = DynamoDbProvider.dynamoDBClient.query(
    json(
        "TableName" to "test-coupling-history",
        "KeyConditionExpression" to "PK = :party",
        "ExpressionAttributeValues" to json(":party" to "ENTITY#PARTY#PARTY#$partyId"),
    ),
).await()["Items"].unsafeCast<Array<Json>>().toList()

private data class TargetPartyConfigurationResult(
    val party: Record<PartyDetails>?,
    val metadata: Json,
)

private data class TargetPartyHistoryResult(
    val current: Record<PartyDetails>?,
    val revisions: List<Json>,
)

private data class TargetPartyAtomicFailureResult(val failure: Throwable?, val metadata: Json?)

private data class TargetPartyTransportFailureResult(
    val commands: List<RecordedDynamoCommand>,
    val failure: Throwable?,
)

private sealed interface RecordedDynamoCommand

private data class RecordedGet(val input: GetItemCommandInput) : RecordedDynamoCommand

private data class RecordedTransaction(val currentMetadataWrite: CurrentMetadataWrite) : RecordedDynamoCommand

private data class CurrentMetadataWrite(val version: Int)

private fun metadata(party: PartyDetails, actorId: String, version: Int) = json(
    "PK" to "PARTY#${party.id.value}",
    "SK" to "META",
    "id" to party.id.value.toString(),
    "name" to party.name,
    "email" to party.email,
    "pairingRule" to PairingRule.toValue(party.pairingRule),
    "badgesEnabled" to party.badgesEnabled,
    "defaultBadgeName" to party.defaultBadgeName,
    "alternateBadgeName" to party.alternateBadgeName,
    "callSignsEnabled" to party.callSignsEnabled,
    "animationEnabled" to party.animationEnabled,
    "animationSpeed" to party.animationSpeed,
    "imageURL" to party.imageURL,
    "modifyingUserId" to actorId,
    "timestamp" to "20261029120000.000",
    "version" to version,
)

private fun transactionCanceledAtCoreVersion() = js(
    "Object.assign(new Error('Core version condition failed'), { name: 'TransactionCanceledException', " +
        "CancellationReasons: [{ Code: 'ConditionalCheckFailed' }] })",
).unsafeCast<Throwable>()

private fun transactionCanceledAtHistoryRevision() = js(
    "Object.assign(new Error('History revision condition failed'), { name: 'TransactionCanceledException', " +
        "CancellationReasons: [{ Code: 'None' }, { Code: 'ConditionalCheckFailed' }] })",
).unsafeCast<Throwable>()

private fun recordingDynamoSyntax(
    commands: MutableList<RecordedDynamoCommand>,
    outcomes: MutableList<Promise<Json>>,
) = object : DynamoDBSyntax {
    override val dynamoDB = js("({})").unsafeCast<DynamoDB>()
    override val dynamoDBClient = js("({})").unsafeCast<DynamoDBDocumentClient>().also { client ->
        client.asDynamic().send = { command: Any ->
            commands.add(command.recorded())
            outcomes.removeAt(0)
        }
    }
}

private fun Any.recorded() = when (this) {
    is TypedGetCommand -> RecordedGet(input)
    is TransactWriteCommand -> RecordedTransaction(input.currentMetadataWrite())
    else -> error("Unexpected DynamoDB command: $this")
}

private fun TransactWriteCommandInput.currentMetadataWrite() = CurrentMetadataWrite(
    transactItems
        .first()
        .put
        .item["version"]
        .unsafeCast<Number>()
        .toInt(),
)

private fun Throwable.name() = unsafeCast<TransactionCanceledError>().name
