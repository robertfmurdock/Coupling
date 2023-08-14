package com.zegreatrob.coupling.action.stats

import com.zegreatrob.coupling.model.pairassignmentdocument.CouplingPair
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pairassignmentdocument.TimeResult
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.testmints.action.SimpleExecutableAction
import kotlinx.datetime.Instant
import kotlin.math.floor
import kotlin.time.Duration

data class ComposeStatisticsAction(
    val party: PartyDetails,
    val players: List<Player>,
    val history: List<PairAssignmentDocument>,
) : SimpleExecutableAction<ComposeStatisticsAction.Dispatcher, StatisticsReport> {
    override val performFunc = link(Dispatcher::perform)

    interface Dispatcher {
        fun perform(action: ComposeStatisticsAction) = StatisticsReport(
            spinsUntilFullRotation = action.players.calculateFullRotation(),
            medianSpinDuration = action.history.medianSpinDuration(),
        )
    }
}

data class StatisticsReport(
    val spinsUntilFullRotation: Int,
    val medianSpinDuration: Duration?,
)

data class PairReport(val pair: CouplingPair.Double, val timeSinceLastPair: TimeResult)

fun List<PairAssignmentDocument>.medianSpinDuration() = asDateTimes()
    .toDeltas()
    .sorted()
    .halfwayValue()

fun List<PairAssignmentDocument>.asDateTimes() = map(PairAssignmentDocument::date)

fun List<Instant>.toDeltas() = zipWithNext { a: Instant, b: Instant -> a - b }

fun List<Duration>.halfwayValue() = getOrNull(indexOfMedian())

fun List<Duration>.indexOfMedian() = floor(size / 2.0).toInt()

fun List<Player>.calculateFullRotation() = size.ifEvenSubtractOne()

private fun Int.ifEvenSubtractOne() = if (this % 2 == 0) {
    this - 1
} else {
    this
}
