import kotlin.js.JsName

data class Game(val history: List<HistoryDocument>, val players: List<Player>, val rule: PairingRule)

data class GameSpin(val history: List<HistoryDocument>, val remainingPlayers: List<Player>, val rule: PairingRule)

data class Round(val pairs: List<CouplingPair>, val gameSpin: GameSpin)

data class SpinCommand(val game: Game)

interface SpinCommandDispatcher {

    val actionDispatcher: GetNextPairActionDispatcher
    val wheel: Wheel

    @JsName("runSpinCommand")
    fun runSpinCommand(history: List<HistoryDocument>, players: Array<Player>, rule: PairingRule) =
            SpinCommand(Game(history, players.toList(), rule))
                    .perform()
                    .map { it.asArray() }
                    .toTypedArray()

    fun SpinCommand.perform() = Round(listOf(), game.nextSpin(game.players))
            .spinForNextPair()

    private fun Round.spinForNextPair(): List<CouplingPair> = getNextPlayer()
            ?.let { playerReport -> continueSpinning(playerReport) }
            ?: pairs

    private fun Round.continueSpinning(playerReport: PairCandidateReport) = playerReport.spinForPartner()
            .let { newPair -> nextRound(newPair) }
            .spinForNextPair()

    private fun Round.getNextPlayer() = GetNextPairAction(gameSpin)
            .performThis()

    private fun Round.nextRound(newPair: CouplingPair) = Round(
            pairs.plus(newPair),
            gameSpin.copyWithout(newPair)
    )

    private fun GameSpin.copyWithout(newPair: CouplingPair) = copy(
            remainingPlayers = remainingPlayers.minus(newPair.asArray())
    )

    private fun PairCandidateReport.spinForPartner(): CouplingPair {
        return if (partners.isEmpty()) {
            CouplingPair.Single(player)
        } else {
            partners.spin()
                    .let { partner -> CouplingPair.Double(player, partner) }
        }
    }

    private fun List<Player>.spin() = with(wheel) { toTypedArray().spin() }

    private fun GetNextPairAction.performThis() = with(actionDispatcher) { perform() }

    private fun Game.nextSpin(remainingPlayers: List<Player>) = GameSpin(history, remainingPlayers, rule)

}

