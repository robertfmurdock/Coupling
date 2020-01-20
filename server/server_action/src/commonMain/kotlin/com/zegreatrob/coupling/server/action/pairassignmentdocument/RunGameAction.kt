package com.zegreatrob.coupling.server.action.pairassignmentdocument

import com.soywiz.klock.DateTime
import com.zegreatrob.coupling.model.pairassignmentdocument.*
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.tribe.Tribe

data class RunGameAction(
    val players: List<Player>,
    val pins: List<Pin>,
    val history: List<PairAssignmentDocument>,
    val tribe: Tribe
)

interface RunGameActionDispatcher : Clock, FindNewPairsActionDispatcher, AssignPinsActionDispatcher {

    fun RunGameAction.perform() = findNewPairs()
        .let { assignPinsToPairs(it, pins) }
        .let(::pairAssignmentDocument)

    private fun RunGameAction.findNewPairs() = findNewPairsAction().perform()

    private fun RunGameAction.findNewPairsAction() = FindNewPairsAction(Game(history, players, tribe.pairingRule))

    private fun assignPinsToPairs(pairs: List<CouplingPair>, pins: List<Pin>) = AssignPinsAction(pairs, pins).perform()

    private fun pairAssignmentDocument(pairAssignments: List<PinnedCouplingPair>) = PairAssignmentDocument(
        currentDate(),
        pairAssignments
    )
}

interface Clock {
    fun currentDate() = DateTime.now()
}

