package com.zegreatrob.coupling.model.pairassignmentdocument

fun calculateTimeSinceLastPartnership(pair: CouplingPair, history: List<PairAssignmentDocument>): TimeResult {
    val documentsSinceLastPartnership =
        history.indexOfFirst { historyDocument -> historyDocument.hasPair(pair) }

    return if (documentsSinceLastPartnership < 0) {
        NeverPaired
    } else {
        TimeResultValue(documentsSinceLastPartnership)
    }
}
