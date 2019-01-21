@JsName("pairingTimeCalculator")
fun pairingTimeCalculator(couplingComparisionSyntax: CouplingComparisionSyntax) = object : PairingTimeCalculationSyntax {
    override val couplingComparisionSyntax: CouplingComparisionSyntax get() = couplingComparisionSyntax
}

interface PairingTimeCalculationSyntax {

    val couplingComparisionSyntax: CouplingComparisionSyntax

    @JsName("calculateTimeSinceLastPartnership")
    fun calculateTimeSinceLastPartnership(pair: CouplingPair, history: List<HistoryDocument>): TimeResult {
        val documentsSinceLastPartnership = history.indexOfFirst { historyDocument -> pairingExistsInDocument(historyDocument, pair) }

        return if (documentsSinceLastPartnership < 0)
            NeverPaired
        else
            TimeResultValue(documentsSinceLastPartnership)
    }

    private fun pairingExistsInDocument(historyDocument: HistoryDocument, pair: CouplingPair) =
            historyDocument.pairs.any { couplingComparisionSyntax.areEqualPairs(pair, it) }
}

external interface CouplingComparisionSyntax {
    fun areEqualPairs(pair1: CouplingPair, pair2: CouplingPair): Boolean
}

sealed class TimeResult

data class TimeResultValue(val time: Int) : TimeResult()

object NeverPaired : TimeResult()

data class HistoryDocument(val pairs: List<CouplingPair>)

sealed class CouplingPair {
    @JsName("asArray")
    abstract fun asArray(): Array<Player>

    object Empty : CouplingPair() {
        override fun asArray() = arrayOf<Player>()
    }

    data class Single(val player: Player) : CouplingPair() {
        override fun asArray() = arrayOf(player)
    }

    data class Double internal constructor(val players: Set<Player>) : CouplingPair() {
        constructor(player1: Player, player2: Player) : this(setOf(player1, player2))

        override fun asArray() = players.toTypedArray()
    }
}

