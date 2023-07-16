package com.zegreatrob.coupling.repository.dynamo

import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocumentId
import com.zegreatrob.coupling.model.pairassignmentdocument.PinnedCouplingPair
import com.zegreatrob.coupling.model.pairassignmentdocument.PinnedPlayer
import com.zegreatrob.coupling.model.party.PartyElement
import kotlinx.datetime.Instant
import kotlin.js.Json
import kotlin.js.json

interface DynamoPairAssignmentDocumentJsonMapping :
    PartyIdDynamoRecordJsonMapping,
    DynamoPlayerJsonMapping,
    DynamoPinJsonMapping {

    private fun PairAssignmentDocument.toDynamoJson() = json(
        "id" to id.value,
        "date" to "${date.toEpochMilliseconds()}",
        "pairs" to pairs.map { it.toDynamoJson() }
            .toTypedArray(),
    )

    fun Record<PartyElement<PairAssignmentDocument>>.asDynamoJson() = recordJson()
        .add(
            json(
                "tribeId" to data.partyId.value,
                "timestamp+id" to "${timestamp.isoWithMillis()}+${data.element.id.value}",
            ),
        )
        .add(data.element.toDynamoJson())

    private fun PinnedCouplingPair.toDynamoJson() = json(
        "pins" to pins.map { it.toDynamoJson() }.toTypedArray(),
        "players" to pinnedPlayers.map { it.toDynamoJson() }.toTypedArray(),
    )

    private fun PinnedPlayer.toDynamoJson() = json(
        "pins" to pins.map { it.toDynamoJson() }.toTypedArray(),
        "player" to player.toDynamoJson(),
    )

    fun Json.toPairAssignmentDocument() = PairAssignmentDocument(
        id = PairAssignmentDocumentId(getDynamoStringValue("id") ?: ""),
        date = getDynamoStringValue("date")?.toLong()?.let { Instant.fromEpochMilliseconds(it) } ?: Instant.DISTANT_PAST,
        pairs = getDynamoListValue("pairs")?.map { pair -> toPinnedCouplingPair(pair) } ?: emptyList(),
    )

    private fun toPinnedCouplingPair(pair: Json) = PinnedCouplingPair(
        pinnedPlayers = pair.getDynamoListValue("players")
            ?.mapNotNull { pinnedPlayerJson -> pinnedPlayerJson.toPinnedPlayer() } ?: emptyList(),
        pins = pair.getDynamoListValue("pins")
            ?.map { pinJson -> pinJson.toPin() }
            ?.toSet()
            ?: emptySet(),
    )

    private fun Json.toPinnedPlayer() = this["player"].unsafeCast<Json>().toPlayer()?.let {
        PinnedPlayer(
            player = it,
            pins = emptyList(),
        )
    }
}
