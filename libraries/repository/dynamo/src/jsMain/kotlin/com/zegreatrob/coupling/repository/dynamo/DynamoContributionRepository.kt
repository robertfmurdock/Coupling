package com.zegreatrob.coupling.repository.dynamo

import com.zegreatrob.coupling.model.Contribution
import com.zegreatrob.coupling.model.PartyRecord
import com.zegreatrob.coupling.model.party.PartyElement
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.party.with
import com.zegreatrob.coupling.model.user.UserIdProvider
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.js.Json
import kotlin.js.json

class DynamoContributionRepository private constructor(override val userId: String, override val clock: Clock) :
    RecordSyntax,
    DynamoRecordJsonMapping,
    UserIdProvider,
    PartyIdDynamoRecordJsonMapping {

    suspend fun save(partyContribution: PartyElement<Contribution>) = performPutItem(
        partyContribution
            .toRecord()
            .asDynamoJson(),
    )

    private fun PartyRecord<Contribution>.asDynamoJson(): Json = recordJson()
        .add(data.toJson())
        .add(
            json(
                "tribeId" to data.partyId.value,
                "timestamp+id" to "${timestamp.isoWithMillis()}+${data.partyId.value}",
            ),
        )

    private fun PartyElement<Contribution>.toJson() = json(
        "id" to element.id,
        "dateTime" to element.dateTime.isoWithMillis(),
        "ease" to element.ease,
        "hash" to element.hash,
        "link" to element.link,
        "participantEmails" to element.participantEmails.toTypedArray(),
        "story" to element.story,
        "contributionTimestamp" to element.timestamp.isoWithMillis(),
    )

    suspend fun get(partyId: PartyId): List<PartyRecord<Contribution>> {
        return partyId.queryForItemList()
            .mapNotNull { toRecord(it) }
            .sortedByDescending { it.data.element.dateTime }
    }

    private fun toRecord(json: Json): PartyRecord<Contribution>? = json.toContribution()
        ?.let { json.tribeId().with(it) }
        ?.let { json.toRecord(it) }

    private fun Json.toContribution(): Contribution? = Contribution(
        id = getDynamoStringValue("id") ?: "",
        dateTime = getDynamoStringValue("date")?.toLong()?.let { Instant.fromEpochMilliseconds(it) }
            ?: Instant.DISTANT_PAST,
        timestamp = getDynamoStringValue("date")?.toLong()?.let { Instant.fromEpochMilliseconds(it) }
            ?: Instant.DISTANT_PAST,
        hash = getDynamoStringValue("hash"),
        ease = getDynamoNumberValue("ease")?.toInt(),
        story = getDynamoStringValue("story"),
        link = getDynamoStringValue("link"),
        participantEmails = getDynamoStringListValue("pairs")?.toList() ?: emptyList(),
    )

    private fun Json.tribeId() = this["tribeId"].unsafeCast<String>().let(::PartyId)

    companion object :
        DynamoRepositoryCreatorSyntax<DynamoContributionRepository>(),
        PartyCreateTableParamProvider,
        DynamoItemPutSyntax,
        PartyIdDynamoItemListGetSyntax,
        DynamoItemPutDeleteRecordSyntax,
        DynamoDBSyntax by DynamoDbProvider {
        override val construct = ::DynamoContributionRepository
        override val tableName = "CONTRIBUTION"
    }
}
