package com.zegreatrob.coupling.model.pairassignmentdocument

import com.zegreatrob.coupling.model.player.Player

interface PairingTimeCalculationSyntax : CouplingComparisionSyntax {

    fun calculateTimeSinceLastPartnership(pair: CouplingPair, history: List<PairAssignmentDocument>): TimeResult {
        val documentsSinceLastPartnership =
            history.indexOfFirst { historyDocument -> pairingExistsInDocument(historyDocument, pair) }

        return if (documentsSinceLastPartnership < 0) {
            NeverPaired
        } else {
            TimeResultValue(documentsSinceLastPartnership)
        }
    }

    private fun pairingExistsInDocument(historyDocument: PairAssignmentDocument, pair: CouplingPair) =
        historyDocument.pairs.any { areEqualPairs(pair, it.toPair()) }
}

interface CouplingComparisionSyntax {
    fun areEqualPairs(pair1: CouplingPair, pair2: CouplingPair) =
        areEqualPairArrays(pair1.asArray(), pair2.asArray())

    private fun areEqualPairArrays(pair1Array: Array<Player>, pair2Array: Array<Player>) =
        fullyEqualPlayers(pair1Array, pair2Array) ||
            equalPlayerIds(pair1Array, pair2Array)

    private fun equalPlayerIds(pair1Array: Array<Player>, pair2Array: Array<Player>) =
        pair1Array.map { it.id }.toSet() == pair2Array.map { it.id }.toSet()

    private fun fullyEqualPlayers(pair1Array: Array<Player>, pair2Array: Array<Player>) =
        pair1Array.toSet() == pair2Array.toSet()
}

sealed class TimeResult

data class TimeResultValue(val time: Int) : TimeResult()

object NeverPaired : TimeResult() {
    override fun toString() = "Never Paired"
}
