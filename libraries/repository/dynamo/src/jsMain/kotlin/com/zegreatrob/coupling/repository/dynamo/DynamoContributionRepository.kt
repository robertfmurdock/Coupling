package com.zegreatrob.coupling.repository.dynamo

import com.zegreatrob.coupling.model.Contribution
import com.zegreatrob.coupling.model.ContributionId
import com.zegreatrob.coupling.model.ContributionQueryParams
import com.zegreatrob.coupling.model.PartyRecord
import com.zegreatrob.coupling.model.party.PartyElement
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.party.with
import com.zegreatrob.coupling.model.user.UserId
import com.zegreatrob.coupling.model.user.UserIdProvider
import com.zegreatrob.coupling.repository.contribution.ContributionRepository
import kotools.types.text.toNotBlankString
import org.kotools.types.ExperimentalKotoolsTypesApi
import kotlin.js.Json
import kotlin.js.json
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Instant

class DynamoContributionRepository private constructor(override val userId: UserId, override val clock: Clock) :
    ContributionRepository,
    RecordSyntax,
    DynamoRecordJsonMapping,
    UserIdProvider,
    PartyIdDynamoRecordJsonMapping {

    override suspend fun save(partyContributions: PartyElement<List<Contribution>>) {
        delete(partyContributions.partyId, partyContributions.element.map { it.id })
        partyContributions.element.map { partyContributions.partyId.with(it) }
            .forEach { partyContribution ->
                performPutItem(
                    partyContribution
                        .toRecord()
                        .asDynamoJson(),
                )
            }
    }

    private suspend fun delete(partyId: PartyId, contributionIds: List<ContributionId>) {
        queryForItemList(
            json(
                "TableName" to prefixedTableName,
                "ScanIndexForward" to false,
                "ExpressionAttributeValues" to json(
                    ":tribeId" to partyId.value.toString(),
                    ":contributionIds" to contributionIds.map { it.value.toString() }.toTypedArray(),
                ),
                "KeyConditionExpression" to "tribeId = :tribeId",
                "FilterExpression" to "contains(:contributionIds, id)",
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

    private fun PartyRecord<Contribution>.sortKeyWithDateTimeFirst() = "${
        (data.element.integrationDateTime ?: data.element.dateTime)?.isoWithMillis()?.let { "dT$it" } ?: timestamp
    }+${data.element.id.value}"

    private fun PartyElement<Contribution>.toJson() = json(
        "id" to element.id.value.toString(),
        "tribeId" to partyId.value.toString(),
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
        "commitCount" to element.commitCount,
        "name" to element.name,
    )

    override suspend fun get(params: ContributionQueryParams) = params.partyId.logAsync("windowedContributions") {
        params.performDynamoQuery()
    }
        .mapNotNull { toRecord(it) }
        .sortedByDescending { "${it.data.element.integrationDateTime ?: it.data.element.dateTime} ${it.data.element.id.value}" }

    private suspend fun ContributionQueryParams.performDynamoQuery() = queryForItemList(
        contributionListQuery(partyId, window, limit),
        limited = limit != null,
    )

    private fun contributionListQuery(partyId: PartyId, window: Duration?, limit: Int?) = if (window != null) {
        json(
            "TableName" to prefixedTableName,
            "ExpressionAttributeValues" to json(
                ":tribeId" to partyId.value.toString(),
                ":windowStart" to (now() - window).isoWithMillis(),
                ":null" to "NULL",
            ),
            "Limit" to limit,
            "ScanIndexForward" to false,
            "KeyConditionExpression" to "tribeId = :tribeId",
            "ExpressionAttributeNames" to json(
                "#dt" to "dateTime",
                "#idt" to "integrationDateTime",
            ),
            "FilterExpression" to "#idt > :windowStart OR (attribute_type(integrationDateTime, :null) AND #dt > :windowStart)",
        )
    } else {
        json(
            "TableName" to prefixedTableName,
            "Limit" to limit,
            "ScanIndexForward" to false,
            "ExpressionAttributeValues" to json(":tribeId" to partyId.value.toString()),
            "KeyConditionExpression" to "tribeId = :tribeId",
        )
    }

    private fun toRecord(json: Json): PartyRecord<Contribution>? = json.toContribution()
        ?.let { json.tribeId().with(it) }
        ?.let { json.toRecord(it) }

    private fun Json.toContribution(): Contribution? {
        return Contribution(
            id = getDynamoStringValue("id")?.toNotBlankString()?.getOrNull()?.let(::ContributionId) ?: return null,
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
            commitCount = getDynamoNumberValue("commitCount")?.toInt(),
            name = getDynamoStringValue("name"),
        )
    }

    @OptIn(ExperimentalKotoolsTypesApi::class)
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
