package com.zegreatrob.coupling.dynamo

import com.soywiz.klock.DateTime
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.pairassignmentdocument.*
import kotlin.js.Json
import kotlin.js.json

interface DynamoPairAssignmentDocumentJsonMapping : TribeIdDynamoRecordJsonMapping, DynamoPlayerJsonMapping,
    DynamoPinJsonMapping {

    private fun PairAssignmentDocument.toDynamoJson() = json(
        "id" to id?.value,
        "date" to "${date.unixMillisLong}",
        "pairs" to pairs.map { it.toDynamoJson() }
            .toTypedArray()
    )

    fun Record<TribeIdPairAssignmentDocument>.asDynamoJson() = recordJson()
        .add(
            json(
                "tribeId" to data.tribeId.value,
                "timestamp+id" to "${timestamp.isoWithMillis()}+${data.element.id?.value}"
            )
        )
        .add(data.element.toDynamoJson())


    private fun PinnedCouplingPair.toDynamoJson() = json(
        "pins" to pins.map { it.toDynamoJson() }.toTypedArray(),
        "players" to players.map { it.toDynamoJson() }.toTypedArray()
    )

    private fun PinnedPlayer.toDynamoJson() = json(
        "pins" to pins.map { it.toDynamoJson() }.toTypedArray(),
        "player" to player.toDynamoJson()
    )

    fun Json.toPairAssignmentDocument() = PairAssignmentDocument(
        id = getDynamoStringValue("id")?.let(::PairAssignmentDocumentId),
        date = getDynamoStringValue("date")?.toLong()?.let { DateTime(it) } ?: throw Exception("Date missing."),
        pairs = getDynamoListValue("pairs")?.map { pair -> toPinnedCouplingPair(pair) } ?: emptyList()
    )

    private fun toPinnedCouplingPair(pair: Json) = PinnedCouplingPair(
        players = pair.getDynamoListValue("players")
            ?.map { pinnedPlayerJson -> pinnedPlayerJson.toPinnedPlayer() } ?: emptyList(),
        pins = pair.getDynamoListValue("pins")
            ?.map { pinJson -> pinJson.toPin() } ?: emptyList()
    )

    private fun Json.toPinnedPlayer() = PinnedPlayer(
        player = this["player"].unsafeCast<Json>().toPlayer(),
        pins = emptyList()
    )


}

