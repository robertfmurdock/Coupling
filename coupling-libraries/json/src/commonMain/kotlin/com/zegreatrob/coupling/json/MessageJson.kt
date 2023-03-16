package com.zegreatrob.coupling.json

import com.zegreatrob.coupling.model.CouplingSocketMessage
import com.zegreatrob.coupling.model.Message
import com.zegreatrob.coupling.model.PairAssignmentAdjustmentMessage
import com.zegreatrob.coupling.model.player.Player
import kotlinx.serialization.Serializable

@Serializable
sealed class JsonMessage

@Serializable
data class JsonCouplingSocketMessage(
    var text: String,
    var players: Set<JsonPlayerData>,
    val currentPairAssignments: JsonPairAssignmentDocument? = null,
) : JsonMessage()

@Serializable
data class JsonPairAssignmentAdjustmentMessage(val currentPairAssignments: JsonPairAssignmentDocument) : JsonMessage()

fun Message.toSerializable() = when (this) {
    is CouplingSocketMessage -> JsonCouplingSocketMessage(
        text = text,
        players = players.map(Player::toSerializable).toSet(),
        currentPairAssignments = currentPairAssignments?.toSerializable(),
    )
    is PairAssignmentAdjustmentMessage -> JsonPairAssignmentAdjustmentMessage(currentPairAssignments.toSerializable())
}

fun JsonMessage.toModel() = when (this) {
    is JsonCouplingSocketMessage -> toModel()
    is JsonPairAssignmentAdjustmentMessage -> toModel()
}

private fun JsonPairAssignmentAdjustmentMessage.toModel() = PairAssignmentAdjustmentMessage(
    currentPairAssignments.toModel(),
)

fun JsonCouplingSocketMessage.toModel() = CouplingSocketMessage(
    text = text,
    players = players.map { it.toModel() }.toSet(),
    currentPairAssignments = currentPairAssignments?.toModel(),
)
