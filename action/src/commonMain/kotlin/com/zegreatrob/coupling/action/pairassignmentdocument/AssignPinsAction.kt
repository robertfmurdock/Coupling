package com.zegreatrob.coupling.action.pairassignmentdocument

import com.zegreatrob.coupling.actionFunc.SimpleExecutableAction
import com.zegreatrob.coupling.model.pairassignmentdocument.*
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.pin.PinTarget

data class AssignPinsAction(
    val pairs: List<CouplingPair>,
    val pins: List<Pin>,
    val history: List<PairAssignmentDocument>
) : SimpleExecutableAction<AssignPinsActionDispatcher, List<PinnedCouplingPair>> {
    override val performFunc = link(AssignPinsActionDispatcher::perform)
}

interface AssignPinsActionDispatcher {
    fun perform(action: AssignPinsAction): List<PinnedCouplingPair> {
        var pinnedPairs = action.pairs.map { it.withPins() }

        action.pins.filter { it.target == PinTarget.Pair }
            .forEach { pin ->
                val pinIterator = listOf(pin).iterator()

                val candidatePairs =
                    findCandidatePairs(pinnedPairs, pin, action.history)

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

    private fun findCandidatePairs(
        pinnedPairs: List<PinnedCouplingPair>,
        pin: Pin,
        history: List<PairAssignmentDocument>
    ): List<PinnedCouplingPair> {
        val groupByLastTime = pinnedPairs.groupBy { pair ->
            val players = pair.asPlayers()

            val lastTimePlayerInPairHadPin = history.indexOfFirst { pairAssignmentDocument ->
                val pairWithPinPlayers = pairWithPinPlayers(pairAssignmentDocument, pin)
                players.fold(false) { foundOne, player ->
                    foundOne || pairWithPinPlayers.contains(player)
                }
            }

            lastTimePlayerInPairHadPin
        }

        val neverPinnedGroup = groupByLastTime[-1]
        if (neverPinnedGroup != null && neverPinnedGroup.isNotEmpty()) {
            val groupByCurrentlyAssignedPins = neverPinnedGroup.groupBy { it.pins.count() }
            return groupByCurrentlyAssignedPins[groupByCurrentlyAssignedPins.keys.min()]!!
        }

        val lastTimePairs = groupByLastTime[groupByLastTime.keys.min()]
            ?: return emptyList()

        val numberOfPinsGroup = lastTimePairs.groupBy { it.pins.count() }
        val leastPinnedOptions = numberOfPinsGroup[numberOfPinsGroup.keys.min()]
        return leastPinnedOptions ?: emptyList()
    }

    private fun pairWithPinPlayers(doc: PairAssignmentDocument, pin: Pin) = playersWithPin(doc, pin)
        ?: emptyList()

    private fun playersWithPin(doc: PairAssignmentDocument, pin: Pin) = pairWithPin(doc, pin)
        ?.asPlayers()

    private fun PinnedCouplingPair.asPlayers() = players.map(
        PinnedPlayer::player)

    private fun pairWithPin(pairAssignmentDocument: PairAssignmentDocument, pin: Pin) =
        pairAssignmentDocument.pairs.find { docPair -> docPair.pins.contains(pin) }
}
