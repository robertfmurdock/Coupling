package com.zegreatrob.coupling.model.pairassignmentdocument

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
    historyDocument.pairs.toList().any { areEqualPairs(pair, it.toPair()) }
