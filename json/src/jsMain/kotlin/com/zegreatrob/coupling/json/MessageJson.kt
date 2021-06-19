package com.zegreatrob.coupling.json

import com.zegreatrob.coupling.model.CouplingSocketMessage
import com.zegreatrob.coupling.model.Message
import com.zegreatrob.coupling.model.PairAssignmentAdjustmentMessage
import kotlin.js.Json
import kotlin.js.json

fun Message.toJson(): Json = when (this) {
    is CouplingSocketMessage -> toJson()
    is PairAssignmentAdjustmentMessage -> json(
        "type" to "PairAssignmentUpdate",
        "currentPairAssignments" to currentPairAssignments.toJson()
    )
}

fun Json.toMessage(): Message? {
    return when (this["type"]) {
        "LivePlayers" -> this.toCouplingServerMessage()
        "PairAssignmentUpdate" -> toPairAssignmentMessage()
        else -> null
    }
}

private fun Json.toPairAssignmentMessage() = PairAssignmentAdjustmentMessage(
    this["currentPairAssignments"].unsafeCast<Json>()
        .toPairAssignmentDocument()
)

fun Json.toCouplingServerMessage() = CouplingSocketMessage(
    this["text"].toString(),
    this["players"].unsafeCast<Array<Json>>().map { it.toPlayer() }.toSet(),
    this["currentPairAssignments"].unsafeCast<Json?>()?.toPairAssignmentDocument()
)

fun CouplingSocketMessage.toJson() = json(
    "type" to "LivePlayers",
    "text" to text,
    "players" to players.map { it.toJson() },
    "currentPairAssignments" to currentPairAssignments?.toJson()
)