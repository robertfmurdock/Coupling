import kotlin.js.Date
import kotlin.js.Json
import kotlin.js.json

data class RunGameAction(
        val players: List<Player>,
        val pins: List<Pin>,
        val history: List<PairAssignmentDocument>,
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

    private fun toJs(it: PairAssignmentDocument) = it.pairs.map {
        it.asArray()
                .map { player ->
                    player.toJson()
                }
                .toTypedArray()
    }
            .toTypedArray()


    val actionDispatcher: FindNewPairsActionDispatcher

    private fun FindNewPairsAction.performThis() = with(actionDispatcher) { perform() }

    fun RunGameAction.perform() = addPinsToPlayers()
            .let { pinnedPlayers -> findNewPairs(pinnedPlayers) }
            .let { pairAssignments -> pairAssignmentDocument(pairAssignments, tribe.id) }

    private fun RunGameAction.findNewPairs(pinnedPlayers: List<Player>) = findNewPairsAction(pinnedPlayers)
            .performThis()

    private fun RunGameAction.findNewPairsAction(pinnedPlayers: List<Player>) = FindNewPairsAction(Game(
            history,
            pinnedPlayers,
            tribe.pairingRule
    ))

    private fun RunGameAction.addPinsToPlayers() = players.assign(pins)

    private fun pairAssignmentDocument(pairAssignments: List<CouplingPair>, tribeId: String) =
            PairAssignmentDocument(
                    currentDate(),
                    pairAssignments,
                    tribeId
            )
}

interface Clock {
    fun currentDate() = Date()
}

