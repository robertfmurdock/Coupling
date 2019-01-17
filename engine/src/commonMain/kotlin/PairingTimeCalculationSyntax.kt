import kotlin.js.JsName

interface PairingTimeCalculationSyntax {

    @JsName("couplingComparisionSyntax")
    val couplingComparisionSyntax: CouplingComparisionSyntax

    @JsName("calculateTimeSinceLastPartnership")
    fun calculateTimeSinceLastPartnership(pair: CouplingPair, history: List<HistoryDocument>): TimeResult {
        val documentsSinceLastPartnership = history.indexOfFirst { historyDocument -> pairingExistsInDocument(historyDocument, pair) }

        return if (documentsSinceLastPartnership < 0)
            NeverPaired
        else
            TimeResultValue(documentsSinceLastPartnership)
    }

    private fun pairingExistsInDocument(historyDocument: HistoryDocument, pair: CouplingPair): Boolean {
        return historyDocument.pairs.any {
            couplingComparisionSyntax.areEqualPairs(pair, it)
        }
    }
}

external interface CouplingComparisionSyntax {
    fun areEqualPairs(pair1: CouplingPair, pair2: CouplingPair): Boolean
}

sealed class TimeResult

data class TimeResultValue(val time: Int) : TimeResult()

object NeverPaired : TimeResult()

external interface PairingDocument {
    val pairs: Array<Array<Player>>?
}

data class HistoryDocument(val pairs: List<CouplingPair>)

@JsName("historyFromArray")
fun historyFromArray(history: Array<PairingDocument>) =
        history.map { HistoryDocument(it.pairs?.map(CouplingPair.Companion::from) ?: listOf()) }

sealed class CouplingPair {
    @JsName("asArray")
    abstract fun asArray(): Array<Player>

    companion object {
        fun from(array: Array<Player>): CouplingPair {
            return when (array.size) {
                1 -> Single(array[0])
                2 -> Double(array[0], array[1])
                else -> Empty
            }
        }
    }

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

