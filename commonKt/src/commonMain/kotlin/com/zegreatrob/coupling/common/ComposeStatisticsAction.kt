package com.zegreatrob.coupling.common

import com.soywiz.klock.DateTime
import com.soywiz.klock.TimeSpan
import com.zegreatrob.coupling.model.pairassignmentdocument.*
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.tribe.KtTribe
import kotlin.math.floor

data class ComposeStatisticsAction(
    val tribe: KtTribe,
    val players: List<Player>,
    val history: List<PairAssignmentDocument>
) : Action

interface ComposeStatisticsActionDispatcher : ActionLoggingSyntax,
    PairingTimeCalculationSyntax {

    fun ComposeStatisticsAction.perform() = log {
        StatisticsReport(
                spinsUntilFullRotation = calculateFullRotation(),
                pairReports = pairReports(),
                medianSpinDuration = history.medianSpinDuration()
        )
    }

    private fun ComposeStatisticsAction.pairReports() = allPairCombinations()
            .map {
                PairReport(
                        it,
                        calculateTimeSinceLastPartnership(it, history)
                )
            }
            .sortedWith(PairReportComparator)


    private fun ComposeStatisticsAction.allPairCombinations() =
            players.mapIndexed { index, player -> players.sliceFrom(index + 1).toPairsWith(player) }
                    .flatten()

    private fun List<Player>.sliceFrom(startIndex: Int) = slice(startIndex..lastIndex)

    private fun List<Player>.toPairsWith(player: Player) =
            map { otherPlayer -> CouplingPair.Double(player, otherPlayer) }

    private fun ComposeStatisticsAction.calculateFullRotation() = players.size.ifEvenSubtractOne()

    private fun Int.ifEvenSubtractOne() = if (this % 2 == 0) {
        this - 1
    } else {
        this
    }

    private fun List<PairAssignmentDocument>.medianSpinDuration() = asDateTimes()
            .toDeltas()
            .sorted()
            .halfwayValue()

    private fun List<TimeSpan>.halfwayValue() = safeGet(indexOfMedian())

    fun List<TimeSpan>.safeGet(indexOfMedian: Int) = indexOfMedian
            .let {
                when {
                    it < size -> this[it]
                    else -> null
                }
            }

    private fun List<TimeSpan>.indexOfMedian() = floor(size / 2.0).toInt()

    private fun List<DateTime>.toDeltas() = zipWithNext { a, b -> a - b }

    private fun List<PairAssignmentDocument>.asDateTimes() = map { it.date }

}

object PairReportComparator : Comparator<PairReport> {

    override fun compare(a: PairReport, b: PairReport) =
            a.timeSinceLastPair.compareTo(b.timeSinceLastPair)

    private fun TimeResult.compareTo(other: TimeResult) = TimeResultComparator.compare(this, other)
}

object TimeResultComparator : Comparator<TimeResult> {
    override fun compare(a: TimeResult, b: TimeResult) = when (a) {
        b -> 0
        is NeverPaired -> -1
        is TimeResultValue -> when (b) {
            is NeverPaired -> 1
            is TimeResultValue -> b.time.compareTo(a.time)
        }
    }
}

data class StatisticsReport(
        val spinsUntilFullRotation: Int,
        val pairReports: List<PairReport>,
        val medianSpinDuration: TimeSpan?
)

data class PairReport(val pair: CouplingPair, val timeSinceLastPair: TimeResult)
