package com.zegreatrob.coupling.server.action.pairassignmentdocument

import com.soywiz.klock.DateTime
import com.zegreatrob.coupling.action.DispatchSyntax
import com.zegreatrob.coupling.action.SimpleSuccessfulExecutableAction
import com.zegreatrob.coupling.action.pairassignmentdocument.AssignPinsAction
import com.zegreatrob.coupling.action.pairassignmentdocument.AssignPinsActionDispatcher
import com.zegreatrob.coupling.model.pairassignmentdocument.CouplingPair
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pairassignmentdocument.PinnedCouplingPair
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.tribe.Tribe

data class RunGameAction(
    val players: List<Player>,
    val pins: List<Pin>,
    val history: List<PairAssignmentDocument>,
    val tribe: Tribe
) : SimpleSuccessfulExecutableAction<RunGameActionDispatcher, PairAssignmentDocument> {
    override val performFunc = link(RunGameActionDispatcher::perform)
}

interface RunGameActionDispatcher : Clock, DispatchSyntax, FindNewPairsActionDispatcher, AssignPinsActionDispatcher {

    fun perform(action: RunGameAction) = action.assignPinsToPairs()
        .let(::pairAssignmentDocument)

    private fun RunGameAction.assignPinsToPairs() = assignPinsToPairs(findNewPairs(), pins, history)

    private fun RunGameAction.findNewPairs() = execute(findNewPairsAction())

    private fun RunGameAction.findNewPairsAction() = FindNewPairsAction(Game(history, players, tribe.pairingRule))

    private fun assignPinsToPairs(pairs: List<CouplingPair>, pins: List<Pin>, history: List<PairAssignmentDocument>) =
        execute(AssignPinsAction(pairs, pins, history))

    private fun pairAssignmentDocument(pairAssignments: List<PinnedCouplingPair>) = PairAssignmentDocument(
        date = currentDate(),
        pairs = pairAssignments
    )
}

interface Clock {
    fun currentDate() = DateTime.now()
}

