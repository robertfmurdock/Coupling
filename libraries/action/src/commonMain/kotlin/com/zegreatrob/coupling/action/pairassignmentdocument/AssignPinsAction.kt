package com.zegreatrob.coupling.action.pairassignmentdocument

import com.zegreatrob.coupling.model.map
import com.zegreatrob.coupling.model.pairassignmentdocument.CouplingPair
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pairassignmentdocument.PinnedCouplingPair
import com.zegreatrob.coupling.model.pairassignmentdocument.players
import com.zegreatrob.coupling.model.pairassignmentdocument.withPins
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.pin.PinTarget
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.testmints.action.SimpleExecutableAction
import kotools.types.collection.NotEmptyList

data class AssignPinsAction(
    val pairs: NotEmptyList<CouplingPair>,
    val pins: List<Pin>,
    val history: List<PairAssignmentDocument>,
) : SimpleExecutableAction<AssignPinsActionDispatcher, NotEmptyList<PinnedCouplingPair>> {
    override val performFunc = link(AssignPinsActionDispatcher::perform)
}

interface AssignPinsActionDispatcher {
    fun perform(action: AssignPinsAction): NotEmptyList<PinnedCouplingPair> {
        var pinnedPairs = action.pairs.map { it.withPins() }

        action.pins.filter { it.target == PinTarget.Pair }
            .forEach { pin ->
                val pinIterator = listOf(pin).iterator()

                val candidatePairs = findPairCandidates(pin, pinnedPairs, action.history)

                pinnedPairs = pinnedPairs.map { pair ->
                    if (candidatePairs.contains(pair) && pinIterator.hasNext()) {
                        pair.copy(pins = pair.pins + pinIterator.next())
                    } else {
                        pair
                    }
                }
            }

        return pinnedPairs
    }

    private fun findPairCandidates(
        pin: Pin,
        pinnedPairs: NotEmptyList<PinnedCouplingPair>,
        history: List<PairAssignmentDocument>,
    ): List<PinnedCouplingPair> {
        val pairsGroupedByLastTime = pinnedPairs.toList().groupBy { pair ->
            lastTimePlayerInPairHadPin(pin, history, pair.players)
        }

        val candidatePairsWhoNeverHadPin = pairsGroupedByLastTime[-1]
            ?.candidatesWithFewestPins()
        if (candidatePairsWhoNeverHadPin != null) {
            return candidatePairsWhoNeverHadPin
        }

        return pairsGroupedByLastTime.minKeyValue()
            ?.candidatesWithFewestPins()
            ?: emptyList()
    }

    private fun List<PinnedCouplingPair>.candidatesWithFewestPins() = groupBy { it.pins.count() }.minKeyValue()

    private fun lastTimePlayerInPairHadPin(
        pin: Pin,
        history: List<PairAssignmentDocument>,
        players: NotEmptyList<Player>,
    ) = history.indexOfFirst { pairAssignmentDocument ->
        val pairWithPinPlayers = pairWithPinPlayers(pairAssignmentDocument, pin)
        players.toList().fold(false) { foundOne, player ->
            foundOne || pairWithPinPlayers?.toList()?.contains(player) == true
        }
    }

    private fun Map<Int, List<PinnedCouplingPair>>.minKeyValue() = this[keys.minOrNull()]

    private fun pairWithPinPlayers(doc: PairAssignmentDocument, pin: Pin) = playersWithPin(doc, pin)

    private fun playersWithPin(doc: PairAssignmentDocument, pin: Pin) = pairWithPin(doc, pin)
        ?.players

    private fun pairWithPin(pairAssignmentDocument: PairAssignmentDocument, pin: Pin) =
        pairAssignmentDocument.pairs.toList().find { docPair -> docPair.pins.contains(pin) }
}
