package com.zegreatrob.coupling.common.entity.heatmap

import com.zegreatrob.coupling.core.entity.pairassignmentdocument.CouplingPair
import com.zegreatrob.coupling.core.entity.pairassignmentdocument.CouplingPair.Companion.equivalent
import com.zegreatrob.coupling.core.entity.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.core.entity.pairassignmentdocument.PinnedCouplingPair
import kotlin.math.min

const val rotationHeatWindow = 5
val heatIncrements = listOf(0.0, 1.0, 2.5, 4.5, 7.0, 10.0)

data class CalculatePairHeatAction(
        val pair: CouplingPair,
        val history: List<PairAssignmentDocument>,
        val rotationPeriod: Int
)

interface CalculatePairHeatActionDispatcher {
    fun CalculatePairHeatAction.perform() = timesPaired()
            .toHeatIncrement()

    private fun CalculatePairHeatAction.timesPaired() = historyInHeatWindow()
            .flattenedPairings()
            .count { equivalent(it, pair) }

    private fun CalculatePairHeatAction.historyInHeatWindow() = history.slice(
            0 until min(lastRelevantRotation, history.size)
    )

    private val CalculatePairHeatAction.lastRelevantRotation get() = rotationPeriod * rotationHeatWindow

    private fun List<PairAssignmentDocument>.flattenedPairings() = map(PairAssignmentDocument::pairs)
            .flatten()
            .map(PinnedCouplingPair::toPair)

    private fun Int.toHeatIncrement() = heatIncrements[incrementIndex(this)]

    private fun incrementIndex(timesPaired: Int) = min(timesPaired, heatIncrements.size - 1)
}