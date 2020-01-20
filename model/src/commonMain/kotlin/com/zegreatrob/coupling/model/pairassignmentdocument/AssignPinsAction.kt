package com.zegreatrob.coupling.model.pairassignmentdocument

import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.pin.PinTarget


data class AssignPinsAction(
    val pairs: List<CouplingPair>,
    val pins: List<Pin>,
    val history: List<PairAssignmentDocument>
)

interface AssignPinsActionDispatcher {
    fun AssignPinsAction.perform(): List<PinnedCouplingPair> {
        var pinnedPairs = pairs.map { it.withPins() }

        pins.filter { it.target == PinTarget.Pair }
            .forEach { pin ->
                val pinIterator = listOf(pin).iterator()

                val candidatePairs = findCandidatePairs(pinnedPairs, pin, history)

                pinnedPairs = pinnedPairs.map { pair ->
                    if (candidatePairs.contains(pair) && pinIterator.hasNext()) {
                        pair.copy(pins = listOf(pinIterator.next()))
                    } else {
                        pair
                    }
                }
            }

        return pinnedPairs
    }

    private fun findCandidatePairs(
        pinnedPairs: List<PinnedCouplingPair>,
        pin: Pin,
        history: List<PairAssignmentDocument>
    ): List<PinnedCouplingPair> {
        val groupByLastTime = pinnedPairs.groupBy { pair ->
            val players = pair.getPlayers()

            val lastTimePlayerInPairHadPin = history.indexOfFirst { pairAssignmentDocument ->
                val pairWithPinPlayers = pairWithPinPlayers(pairAssignmentDocument, pin)
                players.fold(false) { foundOne, player ->
                    foundOne || pairWithPinPlayers.contains(player)
                }
            }

            lastTimePlayerInPairHadPin
        }

        val neverPinnedGroup = groupByLastTime[-1]
        if (neverPinnedGroup?.isNotEmpty() == true) {
            return neverPinnedGroup
        }

        val smallestKey = groupByLastTime.keys.min()

        return groupByLastTime[smallestKey] ?: emptyList()
    }

    private fun pairWithPinPlayers(doc: PairAssignmentDocument, pin: Pin) = playersWithPin(doc, pin)
        ?: emptyList()

    private fun playersWithPin(doc: PairAssignmentDocument, pin: Pin) = pairWithPin(doc, pin)
        ?.getPlayers()

    private fun PinnedCouplingPair.getPlayers() = players.map(PinnedPlayer::player)

    private fun pairWithPin(pairAssignmentDocument: PairAssignmentDocument, pin: Pin) =
        pairAssignmentDocument.pairs.find { docPair -> docPair.pins.contains(pin) }
}
