package com.zegreatrob.coupling.model

import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.player.Player

sealed class Message

data class CouplingSocketMessage(
    var text: String,
    var players: List<Player>,
    val currentPairAssignments: PairAssignmentDocument?
) : Message()

data class PairAssignmentAdjustmentMessage(val currentPairAssignments: PairAssignmentDocument) : Message()