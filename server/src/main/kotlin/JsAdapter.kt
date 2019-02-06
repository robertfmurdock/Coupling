import com.soywiz.klock.internal.toDate
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.promise
import kotlin.js.Json
import kotlin.js.json

@Suppress("unused")
@JsName("performRunGameAction")
fun RunGameActionDispatcher.performRunGameAction(history: Array<Json>, players: Array<Json>, pins: Array<Json>, tribe: Json) =
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
    it.players
            .map { player ->
                player.toJson()
            }
            .toTypedArray()
}
        .toTypedArray()

interface CommandDispatcher : ProposeNewPairsCommandDispatcher,
        PlayersQueryDispatcher,
        SavePlayerCommandDispatcher,
        DeletePlayerCommandDispatcher

@Suppress("unused")
@JsName("commandDispatcher")
fun commandDispatcher(jsRepository: dynamic, username: String): CommandDispatcher = object : CommandDispatcher,
        RunGameActionDispatcher,
        FindNewPairsActionDispatcher,
        NextPlayerActionDispatcher,
        CreatePairCandidateReportsActionDispatcher,
        CreatePairCandidateReportActionDispatcher,
        Wheel {
    override val actionDispatcher = this
    override val wheel: Wheel = this
    override val repository = dataRepository(jsRepository, username)

    @JsName("performSavePlayerCommand")
    fun performSavePlayerCommand(player: Json, tribeId: String) = GlobalScope.promise {
        SavePlayerCommand(TribeIdPlayer(player.toPlayer(), TribeId(tribeId)))
                .perform()
                .let { it.toJson() }
    }

    @JsName("performDeletePlayerCommand")
    fun performDeletePlayerCommand(playerId: String) = GlobalScope.promise {
        DeletePlayerCommand(playerId)
                .perform()
                .let { json() }
    }

    @JsName("performPlayersQuery")
    fun performPlayersQuery(tribeId: String) = GlobalScope.promise {
        PlayersQuery(TribeId(tribeId))
                .perform()
                .map { it.toJson() }
                .toTypedArray()
    }

    @JsName("performProposeNewPairsCommand")
    fun performProposeNewPairsCommand(tribeId: String, players: Array<Json>) = GlobalScope.promise {
        ProposeNewPairsCommand(tribeId, players.map(Json::toPlayer))
                .perform()
                .let {
                    json(
                            "date" to it.date.toDate(),
                            "pairs" to toJs(it),
                            "tribe" to it.tribeId
                    )
                }
    }
}