package com.zegreatrob.coupling.repository.dynamo

import com.zegreatrob.coupling.model.ClockProvider
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.party.PairingRule
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.user.UserId
import com.zegreatrob.coupling.model.user.UserIdProvider
import com.zegreatrob.coupling.repository.dynamo.external.awslibdynamodb.DynamoKey
import com.zegreatrob.coupling.repository.dynamo.external.awslibdynamodb.DynamoPutItemInput
import com.zegreatrob.coupling.repository.dynamo.external.awslibdynamodb.GetItemCommandInput
import com.zegreatrob.coupling.repository.dynamo.external.awslibdynamodb.TransactWriteCommandInput
import com.zegreatrob.coupling.repository.dynamo.external.awslibdynamodb.TransactWriteItem
import com.zegreatrob.coupling.repository.dynamo.external.awslibdynamodb.TransactionCanceledError
import com.zegreatrob.coupling.repository.dynamo.external.getItem
import com.zegreatrob.coupling.repository.dynamo.external.transactWrite
import com.zegreatrob.coupling.repository.party.PartyDetailsGet
import com.zegreatrob.coupling.repository.party.PartyDetailsSave
import kotlinx.coroutines.await
import kotlinx.coroutines.delay
import kotools.types.text.toNotBlankString
import org.kotools.types.ExperimentalKotoolsTypesApi
import kotlin.js.Json
import kotlin.js.json
import kotlin.time.Clock
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Instant

class TargetPartyConfigurationRepository(
    override val userId: UserId,
    override val clock: Clock,
    private val revisionIdGenerator: RevisionIdGenerator = MonotonicUlidRevisionIdGenerator(),
    private val dynamoDBSyntax: DynamoDBSyntax = DynamoDbProvider,
) : PartyDetailsGet,
    PartyDetailsSave,
    UserIdProvider,
    ClockProvider,
    DynamoDatatypeSyntax,
    DynamoDBSyntax by dynamoDBSyntax {

    override suspend fun getDetails(partyId: PartyId) = loadMetadata(partyId)?.record

    override suspend fun save(party: PartyDetails) {
        repeat(MAX_WRITE_ATTEMPTS) { attempt ->
            if (save(party, loadMetadata(party.id))) return
            if (attempt < MAX_WRITE_ATTEMPTS - 1) delay(retryDelay(attempt).milliseconds)
        }
        error("Unable to save party configuration after $MAX_WRITE_ATTEMPTS attempts")
    }

    private suspend fun save(party: PartyDetails, current: CurrentMetadata?): Boolean = try {
        val timestamp = now()
        val revisionId = revisionIdGenerator.next(timestamp)
        dynamoDBClient.transactWrite(
            TransactWriteCommandInput(
                transactItems = arrayOf(
                    TransactWriteItem(put = currentPut(party, current, timestamp)),
                    TransactWriteItem(put = revisionPut(party, current, timestamp, revisionId)),
                ),
            ),
        ).await()
        true
    } catch (error: Throwable) {
        if (error.isConcurrentVersionConflict()) false else throw error
    }

    private suspend fun loadMetadata(partyId: PartyId): CurrentMetadata? = dynamoDBClient.getItem(
        GetItemCommandInput(
            tableName = coreTableName,
            key = DynamoKey(partitionKey = partyPartitionKey(partyId), sortKey = METADATA_SORT_KEY),
            consistentRead = true,
        ),
    ).await().item
        ?.let(::toCurrentMetadata)

    private fun currentPut(party: PartyDetails, current: CurrentMetadata?, timestamp: Instant) = current?.let {
        DynamoPutItemInput(
            tableName = coreTableName,
            item = metadataItem(party, it.nextVersion, timestamp),
            conditionExpression = it.conditionExpression,
            expressionAttributeValues = it.conditionValues,
        )
    } ?: DynamoPutItemInput(
        tableName = coreTableName,
        item = metadataItem(party, current.nextVersion, timestamp),
        conditionExpression = current.conditionExpression,
    )

    private fun revisionPut(
        party: PartyDetails,
        current: CurrentMetadata?,
        timestamp: Instant,
        revisionId: String,
    ) = DynamoPutItemInput(
        tableName = historyTableName,
        item = json(
            "PK" to "ENTITY#PARTY#PARTY#${party.id.value}",
            "SK" to "REV#$revisionId",
            "actorId" to userId.value.toString(),
            "action" to "SAVE_PARTY",
            "timestamp" to timestamp.isoWithMillis(),
            "version" to current.nextVersion,
            "party" to partyJson(party),
        ),
        conditionExpression = "attribute_not_exists(PK)",
    )

    private fun metadataItem(party: PartyDetails, version: Int, timestamp: Instant) = partyJson(party).add(
        json(
            "PK" to partyPartitionKey(party.id),
            "SK" to METADATA_SORT_KEY,
            "modifyingUserId" to userId.value.toString(),
            "timestamp" to timestamp.isoWithMillis(),
            "version" to version,
        ),
    )

    private fun partyJson(party: PartyDetails) = json(
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
    )

    @OptIn(ExperimentalKotoolsTypesApi::class)
    private fun toCurrentMetadata(item: Json) = CurrentMetadata(
        Record(
            PartyDetails(
                id = PartyId(item.getDynamoStringValue("id")?.toNotBlankString()?.getOrThrow()!!),
                pairingRule = PairingRule.fromValue(item.getDynamoNumberValue("pairingRule")?.toInt()),
                badgesEnabled = item.getDynamoBoolValue("badgesEnabled")!!,
                defaultBadgeName = item.getDynamoStringValue("defaultBadgeName")!!,
                alternateBadgeName = item.getDynamoStringValue("alternateBadgeName")!!,
                email = item.getDynamoStringValue("email"),
                name = item.getDynamoStringValue("name"),
                callSignsEnabled = item.getDynamoBoolValue("callSignsEnabled")!!,
                animationEnabled = item.getDynamoBoolValue("animationEnabled")!!,
                animationSpeed = item.getDynamoNumberValue("animationSpeed")!!.toDouble(),
                imageURL = item.getDynamoStringValue("imageURL"),
            ),
            item.getDynamoStringValue("modifyingUserId")?.toNotBlankString()?.getOrNull(),
            false,
            item.getDynamoDateTimeValue("timestamp")!!,
        ),
        item.getDynamoNumberValue("version")!!.toInt(),
    )

    private data class CurrentMetadata(val record: Record<PartyDetails>, val version: Int) {
        val nextVersion get() = version + 1
        val conditionExpression get() = "version = :version"
        val conditionValues get() = json(":version" to version)
    }

    private val CurrentMetadata?.nextVersion get() = this?.nextVersion ?: 1
    private val CurrentMetadata?.conditionExpression get() = this?.conditionExpression ?: "attribute_not_exists(PK)"
    private val coreTableName get() = "${dynamoPrefix()}coupling-core"
    private val historyTableName get() = "${dynamoPrefix()}coupling-history"

    private fun partyPartitionKey(partyId: PartyId) = "PARTY#${partyId.value}"

    private fun retryDelay(attempt: Int) = INITIAL_RETRY_DELAY_MILLIS * (1L shl attempt)

    private companion object {
        const val MAX_WRITE_ATTEMPTS = 3
        const val METADATA_SORT_KEY = "META"
        const val INITIAL_RETRY_DELAY_MILLIS = 10L
    }
}

private fun Throwable.transactionCanceledError() = unsafeCast<TransactionCanceledError>()

private fun Throwable.isConcurrentVersionConflict() = transactionCanceledError().let { error ->
    error.name == "TransactionCanceledException" &&
        error.cancellationReasons
            ?.firstOrNull()
            ?.code == "ConditionalCheckFailed"
}

private fun dynamoPrefix() = js("process.env.DYNAMO_PREFIX").unsafeCast<String?>()
    ?: throw IllegalStateException("No Dynamo Prefix Configured.")
