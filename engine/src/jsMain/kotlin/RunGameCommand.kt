import kotlin.js.Date

class RunGameCommand(
        val players: List<Player>,
        val pins: List<Pin>,
        val history: List<HistoryDocument>,
        val tribe: KtTribe
)

interface RunGameCommandDispatcher : Clock, PinAssignmentSyntax {

    val actionDispatcher: SpinCommandDispatcher

    private fun SpinCommand.performThis() = with(actionDispatcher) { perform() }

    fun RunGameCommand.perform() = SpinCommand(Game(history, players, tribe.pairingRule))
            .performThis()
            .let {
                PairAssignmentDocument(
                        currentDate(),
                        it,
                        tribe.id
                )
            }
}

interface Clock {
    fun currentDate() = Date()
}

