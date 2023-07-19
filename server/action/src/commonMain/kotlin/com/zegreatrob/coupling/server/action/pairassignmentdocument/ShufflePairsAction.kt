package com.zegreatrob.coupling.server.action.pairassignmentdocument

import com.benasher44.uuid.uuid4
import com.zegreatrob.coupling.action.pairassignmentdocument.AssignPinsAction
import com.zegreatrob.coupling.action.pairassignmentdocument.fire
import com.zegreatrob.coupling.model.pairassignmentdocument.CouplingPair
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocumentId
import com.zegreatrob.coupling.model.pairassignmentdocument.PinnedCouplingPair
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.server.action.CannonProvider
import com.zegreatrob.testmints.action.annotation.ActionMint
import kotools.types.collection.NotEmptyList

@ActionMint
data class ShufflePairsAction(
    val party: PartyDetails,
    val players: NotEmptyList<Player>,
    val pins: List<Pin>,
    val history: List<PairAssignmentDocument>,
) {
    interface Dispatcher<out D> : Clock, CannonProvider<D>
        where D : CreatePairCandidateReportAction.Dispatcher,
              D : CreatePairCandidateReportListAction.Dispatcher<D>,
              D : NextPlayerAction.Dispatcher<D>,
              D : FindNewPairsAction.Dispatcher<D>,
              D : AssignPinsAction.Dispatcher {

        suspend fun perform(action: ShufflePairsAction) = action.assignPinsToPairs().let(::pairAssignmentDocument)

        private suspend fun ShufflePairsAction.assignPinsToPairs() = cannon.fire(assignPinsAction(findNewPairs()))
        private suspend fun ShufflePairsAction.findNewPairs() = cannon.fire(findNewPairsAction())

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
