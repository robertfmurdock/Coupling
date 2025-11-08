package com.zegreatrob.coupling.model

import com.zegreatrob.coupling.model.pairassignmentdocument.PairingSet
import com.zegreatrob.coupling.model.player.Player

sealed class Message

data class CouplingSocketMessage(
    var text: String,
    var players: Set<Player>,
    val currentPairAssignments: PairingSet? = null,
) : Message()

data class PairAssignmentAdjustmentMessage(val currentPairAssignments: PairingSet) : Message()
