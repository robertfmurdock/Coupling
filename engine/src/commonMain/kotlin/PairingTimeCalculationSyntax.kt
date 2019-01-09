import kotlin.js.JsName

interface PairingTimeCalculationSyntax {


    fun calculateTimeSinceLastPartnership(pair: CouplingPair, history: List<HistoryDocument>): TimeResult {

        return NeverPaired
    }

    companion object {
        @JsName("historyFromArray")
        fun historyFromArray(history: Array<PairingDocument>) =
                history.map { HistoryDocument(it.pairs?.map(CouplingPair.Companion::from) ?: listOf()) }
    }
}

sealed class TimeResult

data class TimeResultValue(val time: Int) : TimeResult()

object NeverPaired : TimeResult()

external interface PairingDocument {
    val pairs: Array<Array<Player>>?
}

data class HistoryDocument(val pairs: List<CouplingPair>)

sealed class CouplingPair {
    abstract fun asArray(): Array<Player>

    companion object {
        @JsName("from")
        fun from(array: Array<Player>): CouplingPair {
            return when (array.size) {
                1 -> Single(array[0])
                2 -> Double(array[0], array[1])
                else -> Empty
            }
        }
    }
}

object Empty : CouplingPair() {
    override fun asArray() = arrayOf<Player>()
}

data class Single(val player: Player) : CouplingPair() {
    override fun asArray() = arrayOf(player)
}

data class Double(val player1: Player, val player2: Player) : CouplingPair() {
    override fun asArray() = arrayOf(player1, player2)
}