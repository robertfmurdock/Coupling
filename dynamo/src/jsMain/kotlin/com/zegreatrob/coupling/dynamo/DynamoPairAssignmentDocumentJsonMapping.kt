package com.zegreatrob.coupling.dynamo

import com.benasher44.uuid.uuid4
import com.soywiz.klock.DateTime
import com.zegreatrob.coupling.model.pairassignmentdocument.*
import kotlin.js.Json
import kotlin.js.json

interface DynamoPairAssignmentDocumentJsonMapping : TribeIdDynamoRecordJsonMapping, DynamoPlayerJsonMapping,
    DynamoPinJsonMapping {

    fun TribeIdPairAssignmentDocument.toDynamoJson(): Json {
        val docId: String? = document.id?.value ?: "${uuid4()}"
        return tribeId.recordJson(docId).add(json(
            "id" to docId,
            "date" to "${document.date.unixMillisLong}",
            "pairs" to document.pairs.map { it.toDynamoJson() }
                .toTypedArray()
        ))
    }

    private fun PinnedCouplingPair.toDynamoJson() = json(
        "pins" to pins.map { it.toDynamoJson() }.toTypedArray(),
        "players" to players.map { it.toDynamoJson() }.toTypedArray()
    )

    private fun PinnedPlayer.toDynamoJson() = json(
        "pins" to pins.map { it.toDynamoJson() }.toTypedArray(),
        "player" to player.toDynamoJson()
    )

    fun Json.toPairAssignmentDocument() = PairAssignmentDocument(
        id = getDynamoStringValue("id")!!.let(::PairAssignmentDocumentId),
        date = DateTime(getDynamoStringValue("date")!!.toLong()),
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

