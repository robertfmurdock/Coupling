import kotlin.js.JsName

@JsName("pairingTimeCalculator")
fun pairingTimeCalculator() = object : PairingTimeCalculationSyntax {
}

interface PairingTimeCalculationSyntax : CouplingComparisionSyntax {

    @JsName("calculateTimeSinceLastPartnership")
    fun calculateTimeSinceLastPartnership(pair: CouplingPair, history: List<HistoryDocument>): TimeResult {
        val documentsSinceLastPartnership = history.indexOfFirst { historyDocument -> pairingExistsInDocument(historyDocument, pair) }

        return if (documentsSinceLastPartnership < 0)
            NeverPaired
        else
            TimeResultValue(documentsSinceLastPartnership)
    }

    private fun pairingExistsInDocument(historyDocument: HistoryDocument, pair: CouplingPair) =
            historyDocument.pairs.any { areEqualPairs(pair, it) }
}

interface CouplingComparisionSyntax {
    fun areEqualPairs(pair1: CouplingPair, pair2: CouplingPair) =
            areEqualPairArrays(pair1.asArray(), pair2.asArray())

    private fun areEqualPairArrays(pair1Array: Array<Player>, pair2Array: Array<Player>) =
            fullyEqualPlayers(pair1Array, pair2Array)
                    || equalPlayerIds(pair1Array, pair2Array)

    private fun equalPlayerIds(pair1Array: Array<Player>, pair2Array: Array<Player>) =
            pair1Array.map { it._id }.toSet() == pair2Array.map { it._id }.toSet()

    private fun fullyEqualPlayers(pair1Array: Array<Player>, pair2Array: Array<Player>) =
            pair1Array.toSet() == pair2Array.toSet()
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

    data class Double(val player1: Player, val player2: Player) : CouplingPair() {
        override fun asArray() = arrayOf(player1, player2)
    }
}

