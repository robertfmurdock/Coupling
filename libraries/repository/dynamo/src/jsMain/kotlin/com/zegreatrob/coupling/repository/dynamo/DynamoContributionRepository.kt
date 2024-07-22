package com.zegreatrob.coupling.repository.dynamo

import com.zegreatrob.coupling.model.Contribution
import com.zegreatrob.coupling.model.ContributionQueryParams
import com.zegreatrob.coupling.model.PartyRecord
import com.zegreatrob.coupling.model.party.PartyElement
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.party.with
import com.zegreatrob.coupling.model.user.UserIdProvider
import com.zegreatrob.coupling.repository.contribution.ContributionRepository
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.js.Json
import kotlin.js.json
import kotlin.time.Duration

class DynamoContributionRepository private constructor(override val userId: String, override val clock: Clock) :
    ContributionRepository,
    RecordSyntax,
    DynamoRecordJsonMapping,
    UserIdProvider,
    PartyIdDynamoRecordJsonMapping {

    override suspend fun save(partyContribution: PartyElement<Contribution>) {
        delete(partyContribution.partyId, partyContribution.element.id)
        performPutItem(
            partyContribution
                .toRecord()
                .asDynamoJson(),
        )
    }

    private suspend fun delete(partyId: PartyId, contributionId: String) {
        queryForItemList(
            json(
                "TableName" to prefixedTableName,
                "ScanIndexForward" to false,
                "ExpressionAttributeValues" to json(":tribeId" to partyId.value, ":contributionId" to contributionId),
                "KeyConditionExpression" to "tribeId = :tribeId",
                "FilterExpression" to "id = :contributionId",
            ),
            limited = false,
        )
            .forEach { dynamoContribution -> dynamoContribution.performDeleteContribution() }
    }

    override suspend fun deleteAll(partyId: PartyId) = partyId.queryForItemList().forEach { item ->
        item.performDeleteContribution()
    }

    private suspend fun Json.performDeleteContribution() = performDeleteItem(
        json(
            "tribeId" to this["tribeId"],
            "timestamp+id" to this["timestamp+id"],
        ),
    )

    private fun PartyRecord<Contribution>.asDynamoJson(): Json = recordJson()
        .add(data.toJson())
        .add(json("timestamp+id" to sortKeyWithDateTimeFirst()))

    private fun PartyRecord<Contribution>.sortKeyWithDateTimeFirst() =
        "${data.element.dateTime?.isoWithMillis()?.let { "dT$it" } ?: timestamp}+${data.element.id}"

    private fun PartyElement<Contribution>.toJson() = json(
        "id" to element.id,
        "tribeId" to partyId.value,
        "dateTime" to element.dateTime?.isoWithMillis(),
        "ease" to element.ease,
        "hash" to element.hash,
        "link" to element.link,
        "label" to element.label,
        "semver" to element.semver,
        "firstCommit" to element.firstCommit,
        "firstCommitDateTime" to element.firstCommitDateTime?.isoWithMillis(),
        "participantEmails" to element.participantEmails.toTypedArray(),
        "story" to element.story,
        "createdAt" to element.createdAt.isoWithMillis(),
        "integrationDateTime" to element.integrationDateTime?.isoWithMillis(),
        "cycleTime" to element.cycleTime?.toString(),
    )

    override suspend fun get(params: ContributionQueryParams) = params.partyId.logAsync("windowedContributions") {
        params.performDynamoQuery()
    }
        .mapNotNull { toRecord(it) }
        .sortedByDescending { "${it.data.element.dateTime} ${it.data.element.id}" }

    private suspend fun ContributionQueryParams.performDynamoQuery() = queryForItemList(
        contributionListQuery(partyId, window, limit),
        limited = limit != null,
    )

    private fun contributionListQuery(partyId: PartyId, window: Duration?, limit: Int?) = if (window != null) {
        json(
            "TableName" to prefixedTableName,
            "ExpressionAttributeValues" to json(
                ":tribeId" to partyId.value,
                ":windowStart" to (now() - window).isoWithMillis(),
            ),
            "Limit" to limit,
            "ScanIndexForward" to false,
            "KeyConditionExpression" to "tribeId = :tribeId",
            "ExpressionAttributeNames" to json("#dt" to "dateTime"),
            "FilterExpression" to "#dt > :windowStart",
        )
    } else {
        json(
            "TableName" to prefixedTableName,
            "Limit" to limit,
            "ScanIndexForward" to false,
            "ExpressionAttributeValues" to json(":tribeId" to partyId.value),
            "KeyConditionExpression" to "tribeId = :tribeId",
        )
    }

    private fun toRecord(json: Json): PartyRecord<Contribution>? = json.toContribution()
        ?.let { json.tribeId().with(it) }
        ?.let { json.toRecord(it) }

    private fun Json.toContribution(): Contribution? {
        return Contribution(
            id = getDynamoStringValue("id") ?: return null,
            createdAt = getDynamoDateTimeValue("createdAt") ?: Instant.DISTANT_PAST,
            dateTime = getDynamoDateTimeValue("dateTime"),
            hash = getDynamoStringValue("hash"),
            ease = getDynamoNumberValue("ease")?.toInt(),
            story = getDynamoStringValue("story"),
            link = getDynamoStringValue("link"),
            participantEmails = getDynamoStringListValue("participantEmails")?.toSet() ?: emptySet(),
            label = getDynamoStringValue("label"),
            semver = getDynamoStringValue("semver"),
            firstCommit = getDynamoStringValue("firstCommit"),
            firstCommitDateTime = getDynamoDateTimeValue("firstCommitDateTime"),
            integrationDateTime = getDynamoDateTimeValue("integrationDateTime"),
            cycleTime = getDynamoStringValue("cycleTime")?.let(Duration.Companion::parse),
        )
    }

    private fun Json.tribeId() = this["tribeId"].unsafeCast<String>().let(::PartyId)

    companion object :
        DynamoRepositoryCreatorSyntax<DynamoContributionRepository>(),
        PartyCreateTableParamProvider,
        DynamoItemPutSyntax,
        PartyIdDynamoItemListGetSyntax,
        DynamoItemDeleteSyntax,
        DynamoItemPutDeleteRecordSyntax,
        DynamoDBSyntax by DynamoDbProvider {
        override val construct = ::DynamoContributionRepository
        override val tableName = "CONTRIBUTION"
    }
}
