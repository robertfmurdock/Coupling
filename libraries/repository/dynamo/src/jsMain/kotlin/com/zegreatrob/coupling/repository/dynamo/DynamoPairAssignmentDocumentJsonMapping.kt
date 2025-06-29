package com.zegreatrob.coupling.repository.dynamo

import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocumentId
import com.zegreatrob.coupling.model.pairassignmentdocument.PinnedCouplingPair
import com.zegreatrob.coupling.model.pairassignmentdocument.PinnedPlayer
import com.zegreatrob.coupling.model.party.PartyElement
import kotools.types.collection.toNotEmptyList
import kotools.types.text.toNotBlankString
import kotlin.js.Json
import kotlin.js.json
import kotlin.time.Instant

interface DynamoPairAssignmentDocumentJsonMapping :
    PartyIdDynamoRecordJsonMapping,
    DynamoPlayerJsonMapping,
    DynamoPinJsonMapping {

    private fun PairAssignmentDocument.toDynamoJson() = json(
        "id" to id.value.toString(),
        "date" to "${date.toEpochMilliseconds()}",
        "pairs" to pairs.toList().map { it.toDynamoJson() }
            .toTypedArray(),
        "discordMessageId" to discordMessageId,
        "slackMessageId" to slackMessageId,
    )

    fun Record<PartyElement<PairAssignmentDocument>>.asDynamoJson() = recordJson()
        .add(
            json(
                "tribeId" to data.partyId.value.toString(),
                "timestamp+id" to "${timestamp.isoWithMillis()}+${data.element.id.value}",
            ),
        )
        .add(data.element.toDynamoJson())

    private fun PinnedCouplingPair.toDynamoJson() = json(
        "pins" to pins.map { it.toDynamoJson() }.toTypedArray(),
        "players" to pinnedPlayers.toList().map { it.toDynamoJson() }.toTypedArray(),
    )

    private fun PinnedPlayer.toDynamoJson() = json(
        "pins" to pins.map { it.toDynamoJson() }.toTypedArray(),
        "player" to player.toDynamoJson(),
    )

    fun Json.toPairAssignmentDocument(): PairAssignmentDocument? {
        return PairAssignmentDocument(
            id = PairAssignmentDocumentId(getDynamoStringValue("id")?.toNotBlankString()?.getOrNull() ?: return null),
            date = getDynamoStringValue("date")?.toLong()?.let { Instant.fromEpochMilliseconds(it) }
                ?: Instant.DISTANT_PAST,
            pairs = getDynamoListValue("pairs")?.mapNotNull { pair -> toPinnedCouplingPair(pair) }
                ?.toNotEmptyList()
                ?.getOrNull()
                ?: return null,
            discordMessageId = getDynamoStringValue("discordMessageId"),
            slackMessageId = getDynamoStringValue("slackMessageId"),
        )
    }

    private fun toPinnedCouplingPair(pair: Json): PinnedCouplingPair? {
        return PinnedCouplingPair(
            pinnedPlayers = pair.getDynamoListValue("players")
                ?.mapNotNull { pinnedPlayerJson -> pinnedPlayerJson.toPinnedPlayer() }
                ?.toNotEmptyList()?.getOrNull() ?: return null,
            pins = pair.getDynamoListValue("pins")
                ?.mapNotNull { pinJson -> pinJson.toPin() }
                ?.toSet()
                ?: emptySet(),
        )
    }

    private fun Json.toPinnedPlayer() = this["player"].unsafeCast<Json>().toPlayer()?.let {
        PinnedPlayer(
            player = it,
            pins = emptyList(),
        )
    }
}
