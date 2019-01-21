import kotlin.js.Date
import kotlin.js.Json
import kotlin.js.json

class RunGameAction(
        val players: List<Player>,
        val pins: List<Pin>,
        val history: List<HistoryDocument>,
        val tribe: KtTribe
)

interface RunGameActionDispatcher : Clock, PinAssignmentSyntax {

    @JsName("performRunGameCommand")
    fun performRunGameCommand(history: Array<PairingDocument>, players: Array<Json>, pins: Array<Json>, tribe: Json) =
            RunGameAction(
                    players = players.map { it.toPlayer() }.toList(),
                    pins = pins.toPins(),
                    history = historyFromArray(history),
                    tribe = tribe.toTribe()
            )
                    .perform()
                    .let {
                        json(
                                "date" to it.date,
                                "pairs" to toJs(it),
                                "tribe" to it.tribeId
                        )
                    }

    fun toJs(it: PairAssignmentDocument): Array<Array<Json>> {
        return it.expectedPairingAssignments.map {
            it.asArray()
                    .map { player ->
                        player.toJson()
                    }
                    .toTypedArray()
        }
                .toTypedArray()
    }


    val actionDispatcher: SpinActionDispatcher

    private fun SpinAction.performThis() = with(actionDispatcher) { perform() }

    fun RunGameAction.perform() = SpinAction(Game(history, players.assign(pins), tribe.pairingRule))
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

