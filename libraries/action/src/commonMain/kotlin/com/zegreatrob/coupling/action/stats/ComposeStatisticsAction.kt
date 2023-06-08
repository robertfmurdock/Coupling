package com.zegreatrob.coupling.action.stats

import com.zegreatrob.coupling.model.pairassignmentdocument.CouplingPair
import com.zegreatrob.coupling.model.pairassignmentdocument.NeverPaired
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pairassignmentdocument.PairingTimeCalculationSyntax
import com.zegreatrob.coupling.model.pairassignmentdocument.TimeResult
import com.zegreatrob.coupling.model.pairassignmentdocument.TimeResultValue
import com.zegreatrob.coupling.model.pairassignmentdocument.pairOf
import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.testmints.action.SimpleExecutableAction
import korlibs.time.DateTime
import korlibs.time.TimeSpan
import kotlin.math.floor

data class ComposeStatisticsAction(
    val party: Party,
    val players: List<Player>,
    val history: List<PairAssignmentDocument>,
) : SimpleExecutableAction<ComposeStatisticsAction.Dispatcher, StatisticsReport> {
    override val performFunc = link(Dispatcher::perform)

    interface Dispatcher : PairingTimeCalculationSyntax {

        fun perform(action: ComposeStatisticsAction) = StatisticsReport(
            spinsUntilFullRotation = action.players.calculateFullRotation(),
            pairReports = action.pairReports(),
            medianSpinDuration = action.history.medianSpinDuration(),
        )

        private fun ComposeStatisticsAction.pairReports() = players.allPairCombinations()
            .map { PairReport(it, calculateTimeSinceLastPartnership(it, history)) }
            .sortedWith { a, b -> compare(a.timeSinceLastPair, b.timeSinceLastPair) }

        private fun compare(a: TimeResult, b: TimeResult) = when (a) {
            b -> 0
            is NeverPaired -> -1
            is TimeResultValue -> when (b) {
                is NeverPaired -> 1
                is TimeResultValue -> b.time.compareTo(a.time)
            }
        }

        private fun List<Player>.allPairCombinations() = mapIndexed { index, player ->
            slice(index + 1..lastIndex).toPairsWith(player)
        }.flatten()

        private fun List<Player>.toPairsWith(player: Player) = map { otherPlayer -> pairOf(player, otherPlayer) }

        private fun List<Player>.calculateFullRotation() = size.ifEvenSubtractOne()

        private fun Int.ifEvenSubtractOne() = if (this % 2 == 0) {
            this - 1
        } else {
            this
        }
    }
}

data class StatisticsReport(
    val spinsUntilFullRotation: Int,
    val pairReports: List<PairReport>,
    val medianSpinDuration: TimeSpan?,
)

data class PairReport(val pair: CouplingPair.Double, val timeSinceLastPair: TimeResult)

fun List<PairAssignmentDocument>.medianSpinDuration() = asDateTimes()
    .toDeltas()
    .sorted()
    .halfwayValue()

fun List<PairAssignmentDocument>.asDateTimes() = map(PairAssignmentDocument::date)

fun List<DateTime>.toDeltas() = zipWithNext { a, b -> a - b }

fun List<TimeSpan>.halfwayValue() = getOrNull(indexOfMedian())

fun List<TimeSpan>.indexOfMedian() = floor(size / 2.0).toInt()