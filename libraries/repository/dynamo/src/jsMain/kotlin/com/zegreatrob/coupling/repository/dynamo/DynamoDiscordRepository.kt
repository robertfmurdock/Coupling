package com.zegreatrob.coupling.repository.dynamo

import com.zegreatrob.coupling.model.DiscordTeamAccess
import com.zegreatrob.coupling.model.DiscordWebhook
import com.zegreatrob.coupling.model.PartyRecord
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.party.PartyElement
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.party.with
import com.zegreatrob.coupling.model.user.UserId
import com.zegreatrob.coupling.model.user.UserIdProvider
import com.zegreatrob.coupling.repository.discord.DiscordAccessRepository
import org.kotools.types.ExperimentalKotoolsTypesApi
import kotlin.js.Json
import kotlin.js.json
import kotlin.time.Clock

class DynamoDiscordRepository private constructor(override val userId: UserId, override val clock: Clock) :
    RecordSyntax,
    DynamoRecordJsonMapping,
    UserIdProvider,
    PartyIdDynamoRecordJsonMapping,
    DiscordAccessRepository {
    override suspend fun save(partyDiscordAccess: PartyElement<DiscordTeamAccess>) = performPutItem(
        partyDiscordAccess
            .toRecord()
            .asDynamoJson(),
    )

    override suspend fun get(partyId: PartyId): PartyRecord<DiscordTeamAccess>? = performGetSingleItemQuery(partyId.value.toString(), partyId)
        ?.toRecord()

    @OptIn(ExperimentalKotoolsTypesApi::class)
    private fun Json.toRecord(): Record<PartyElement<DiscordTeamAccess>> {
        val partyId = this["tribeId"].unsafeCast<String>().let(::PartyId)
        return toRecord(partyId.with(toAccess()))
    }

    companion object :
        DynamoRepositoryCreatorSyntax<DynamoDiscordRepository>(),
        PartyCreateTableParamProvider,
        DynamoItemPutSyntax,
        PartyIdDynamoItemListGetSyntax,
        DynamoItemPutDeleteRecordSyntax,
        DynamoDBSyntax by DynamoDbProvider {
        override val construct = ::DynamoDiscordRepository
        override val tableName = "DISCORD"
    }

    private fun PartyRecord<DiscordTeamAccess>.asDynamoJson(): Json = recordJson()
        .add(data.toJson())
        .add(
            json(
                "tribeId" to data.partyId.value.toString(),
                "timestamp+id" to "${timestamp.isoWithMillis()}+${data.partyId.value}",
            ),
        )

    private fun PartyElement<DiscordTeamAccess>.toJson() = json(
        "id" to partyId.value.toString(),
        "accessToken" to element.accessToken,
        "refreshToken" to element.refreshToken,
        "webhookId" to element.webhook.id,
        "webhookToken" to element.webhook.token,
        "webhookChannelId" to element.webhook.channelId,
        "webhookGuildId" to element.webhook.guildId,
    )

    private fun Json.toAccess(): DiscordTeamAccess = DiscordTeamAccess(
        accessToken = getDynamoStringValue("accessToken") ?: "",
        refreshToken = getDynamoStringValue("refreshToken") ?: "",
        webhook = DiscordWebhook(
            id = getDynamoStringValue("webhookId") ?: "",
            token = getDynamoStringValue("webhookToken") ?: "",
            channelId = getDynamoStringValue("webhookChannelId") ?: "",
            guildId = getDynamoStringValue("webhookGuildId") ?: "",
        ),
    )
}
