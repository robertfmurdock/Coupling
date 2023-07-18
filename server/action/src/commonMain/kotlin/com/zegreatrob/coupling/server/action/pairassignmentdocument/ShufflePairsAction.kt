package com.zegreatrob.coupling.server.action.pairassignmentdocument

import com.benasher44.uuid.uuid4
import com.zegreatrob.coupling.action.pairassignmentdocument.AssignPinsAction
import com.zegreatrob.coupling.action.pairassignmentdocument.AssignPinsActionDispatcher
import com.zegreatrob.coupling.model.pairassignmentdocument.CouplingPair
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocumentId
import com.zegreatrob.coupling.model.pairassignmentdocument.PinnedCouplingPair
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.server.action.CannonProvider
import com.zegreatrob.testmints.action.ExecutableActionExecuteSyntax
import com.zegreatrob.testmints.action.async.SimpleSuspendAction
import kotools.types.collection.NotEmptyList

data class ShufflePairsAction(
    val party: PartyDetails,
    val players: NotEmptyList<Player>,
    val pins: List<Pin>,
    val history: List<PairAssignmentDocument>,
) : SimpleSuspendAction<ShufflePairsAction.Dispatcher<*>, PairAssignmentDocument> {
    override val performFunc = link(Dispatcher<*>::perform)

    interface Dispatcher<out D> :
        Clock,
        CannonProvider<D>,
        ExecutableActionExecuteSyntax,
        FindNewPairsAction.Dispatcher,
        AssignPinsActionDispatcher {

        fun perform(action: ShufflePairsAction) = action.assignPinsToPairs().let(::pairAssignmentDocument)

        private fun ShufflePairsAction.assignPinsToPairs() = assignPins(findNewPairs())
        private fun ShufflePairsAction.assignPins(pairs: NotEmptyList<CouplingPair>) = execute(assignPinsAction(pairs))
        private fun ShufflePairsAction.findNewPairs() = execute(findNewPairsAction())

        private fun ShufflePairsAction.assignPinsAction(pairs: NotEmptyList<CouplingPair>) =
            AssignPinsAction(pairs, pins, history)

        private fun ShufflePairsAction.findNewPairsAction() =
            FindNewPairsAction(Game(players, history, party.pairingRule))

        private fun pairAssignmentDocument(pairAssignments: NotEmptyList<PinnedCouplingPair>) = PairAssignmentDocument(
            id = PairAssignmentDocumentId("${uuid4()}"),
            date = currentDate(),
            pairs = pairAssignments,
        )
    }
}
