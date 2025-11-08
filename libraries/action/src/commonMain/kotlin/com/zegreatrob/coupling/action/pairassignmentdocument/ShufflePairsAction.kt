package com.zegreatrob.coupling.action.pairassignmentdocument

import com.zegreatrob.coupling.action.CannonProvider
import com.zegreatrob.coupling.model.pairassignmentdocument.CouplingPair
import com.zegreatrob.coupling.model.pairassignmentdocument.PairingSet
import com.zegreatrob.coupling.model.pairassignmentdocument.PairingSetId
import com.zegreatrob.coupling.model.pairassignmentdocument.PinnedCouplingPair
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.testmints.action.annotation.ActionMint
import kotools.types.collection.NotEmptyList

@ActionMint
data class ShufflePairsAction(
    val party: PartyDetails,
    val players: NotEmptyList<Player>,
    val pins: List<Pin>,
    val history: List<PairingSet>,
) {
    interface Dispatcher<out D> :
        Clock,
        CannonProvider<D>
        where D : CreatePairCandidateReportAction.Dispatcher,
              D : CreatePairCandidateReportListAction.Dispatcher<D>,
              D : NextPlayerAction.Dispatcher<D>,
              D : FindNewPairsAction.Dispatcher<D>,
              D : AssignPinsAction.Dispatcher {

        suspend fun perform(action: ShufflePairsAction) = action.assignPinsToPairs().let(::pairAssignmentDocument)

        private suspend fun ShufflePairsAction.assignPinsToPairs(): NotEmptyList<PinnedCouplingPair> = cannon.fire(assignPinsAction(findNewPairs()))

        private suspend fun ShufflePairsAction.findNewPairs(): NotEmptyList<CouplingPair> = cannon.fire(findNewPairsAction())

        private fun ShufflePairsAction.assignPinsAction(pairs: NotEmptyList<CouplingPair>) = AssignPinsAction(pairs, pins, history)

        private fun ShufflePairsAction.findNewPairsAction() = FindNewPairsAction(Game(players, history, party.pairingRule))

        private fun pairAssignmentDocument(pairAssignments: NotEmptyList<PinnedCouplingPair>) = PairingSet(
            id = PairingSetId.new(),
            date = currentDate(),
            pairs = pairAssignments,
            discordMessageId = null,
        )
    }
}
