package com.zegreatrob.coupling.action.stats

import com.zegreatrob.coupling.model.pairassignmentdocument.CouplingPair
import com.zegreatrob.coupling.model.pairassignmentdocument.PairingSet
import com.zegreatrob.coupling.model.pairassignmentdocument.TimeResult
import com.zegreatrob.coupling.model.player.Player
import kotlin.math.floor
import kotlin.time.Duration
import kotlin.time.Instant

data class PairReport(val pair: CouplingPair.Double, val timeSinceLastPair: TimeResult)

fun List<PairingSet>.medianSpinDuration() = asDateTimes()
    .toDeltas()
    .sorted()
    .halfwayValue()

fun List<PairingSet>.asDateTimes() = map(PairingSet::date)

fun List<Instant>.toDeltas() = zipWithNext { a: Instant, b: Instant -> a - b }

fun List<Duration>.halfwayValue() = getOrNull(indexOfMedian())

fun List<Duration>.indexOfMedian() = floor(size / 2.0).toInt()

fun List<Player>.spinsUntilFullRotation() = size.ifEvenSubtractOne()

private fun Int.ifEvenSubtractOne() = if (this % 2 == 0) {
    this - 1
} else {
    this
}
