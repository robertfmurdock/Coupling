package com.zegreatrob.coupling.action.stats.heatmap

const val RECENT_PAIRING_ROTATION_CYCLES = 5

data class PairingBaseline(val savedSpinsInWindow: Int, val spinsUntilFullRotation: Int) {
    val expectedPairings: Double get() = savedSpinsInWindow.toDouble() / spinsUntilFullRotation
}

fun pairingBaseline(savedSpinCount: Int?, spinsUntilFullRotation: Int): PairingBaseline? {
    if (savedSpinCount == null || savedSpinCount < 0 || spinsUntilFullRotation <= 0) return null
    val windowSize = spinsUntilFullRotation.toLong() * RECENT_PAIRING_ROTATION_CYCLES
    return PairingBaseline(minOf(savedSpinCount.toLong(), windowSize).toInt(), spinsUntilFullRotation)
}
