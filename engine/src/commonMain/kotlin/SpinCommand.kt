
data class Game(val history: List<HistoryDocument>, val players: List<Player>, val rule: PairingRule)

data class SpinCommand(val game: Game)

interface SpinCommandDispatcher {

}