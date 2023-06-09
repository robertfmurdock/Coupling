package com.zegreatrob.coupling.server.action.pairassignmentdocument

import com.benasher44.uuid.uuid4
import com.zegreatrob.coupling.action.pairassignmentdocument.AssignPinsAction
import com.zegreatrob.coupling.action.pairassignmentdocument.AssignPinsActionDispatcher
import com.zegreatrob.coupling.model.pairassignmentdocument.CouplingPair
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocumentId
import com.zegreatrob.coupling.model.pairassignmentdocument.PinnedCouplingPair
import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.testmints.action.ExecutableActionExecuteSyntax
import com.zegreatrob.testmints.action.SimpleExecutableAction

data class ShufflePairsAction(
    val party: Party,
    val players: List<Player>,
    val pins: List<Pin>,
    val history: List<PairAssignmentDocument>,
) : SimpleExecutableAction<ShufflePairsAction.Dispatcher, PairAssignmentDocument> {
    override val performFunc = link(Dispatcher::perform)

    interface Dispatcher :
        Clock,
        ExecutableActionExecuteSyntax,
        FindNewPairsAction.Dispatcher,
        AssignPinsActionDispatcher {

        fun perform(action: ShufflePairsAction) = action.assignPinsToPairs().let(::pairAssignmentDocument)

        private fun ShufflePairsAction.assignPinsToPairs() = assignPins(findNewPairs())
        private fun ShufflePairsAction.assignPins(pairs: List<CouplingPair>) = execute(assignPinsAction(pairs))
        private fun ShufflePairsAction.findNewPairs() = execute(findNewPairsAction())

        private fun ShufflePairsAction.assignPinsAction(pairs: List<CouplingPair>) =
            AssignPinsAction(pairs, pins, history)

        private fun ShufflePairsAction.findNewPairsAction() =
            FindNewPairsAction(Game(history, players, party.pairingRule))

        private fun pairAssignmentDocument(pairAssignments: List<PinnedCouplingPair>) = PairAssignmentDocument(
            id = PairAssignmentDocumentId("${uuid4()}"),
            date = currentDate(),
            pairs = pairAssignments,
        )
    }
}
