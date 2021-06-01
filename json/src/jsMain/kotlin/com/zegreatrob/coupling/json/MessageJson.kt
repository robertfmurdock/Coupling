package com.zegreatrob.coupling.json

import com.zegreatrob.coupling.model.CouplingSocketMessage
import com.zegreatrob.coupling.model.Message
import com.zegreatrob.coupling.model.PairAssignmentAdjustmentMessage
import kotlin.js.Json
import kotlin.js.json

fun Message.toJson(): Json {
    return when (this) {
        is CouplingSocketMessage -> toJson()
        is PairAssignmentAdjustmentMessage -> json("currentPairAssignments" to currentPairAssignments.toJson())
    }
}

fun toCouplingServerMessage(json: Json) = CouplingSocketMessage(
    json["text"].toString(),
    json["players"].unsafeCast<Array<Json>>().map { it.toPlayer() }.toSet(),
    json["currentPairAssignments"].unsafeCast<Json?>()?.toPairAssignmentDocument()
)

fun CouplingSocketMessage.toJson() = json(
    "type" to "LivePlayers",
    "text" to text,
    "players" to players.map { it.toJson() },
    "currentPairAssignments" to currentPairAssignments?.toJson()
)