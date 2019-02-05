import com.soywiz.klock.internal.toDate
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.promise
import kotlin.js.Json
import kotlin.js.json

@JsName("performRunGameCommand")
fun RunGameActionDispatcher.performRunGameCommand(history: Array<Json>, players: Array<Json>, pins: Array<Json>, tribe: Json) =
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

@Suppress("unused")
@JsName("proposeNewPairsCommandDispatcher")
fun proposeNewPairsCommandDispatcher(jsRepository: dynamic): ProposeNewPairsCommandDispatcher = object :
        ProposeNewPairsCommandDispatcher,
        FindNewPairsActionDispatcher,
        NextPlayerActionDispatcher,
        CreatePairCandidateReportActionDispatcher,
        CreatePairCandidateReportsActionDispatcher,
        RunGameActionDispatcher,
        Wheel {
    override val repository = dataRepository(jsRepository)
    override val actionDispatcher = this
    override val wheel = this

    @Suppress("unused")
    @JsName("performCommand")
    fun performCommand(tribeId: String, players: Array<Json>) =
            GlobalScope.promise {
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

@Suppress("unused")
@JsName("playersQueryDispatcher")
fun playersQueryDispatcher(jsRepository: dynamic): PlayersQueryDispatcher = object : PlayersQueryDispatcher {
    override val repository = dataRepository(jsRepository)

    @Suppress("unused")
    @JsName("performQuery")
    fun performQuery(tribeId: String) = GlobalScope.promise {
        PlayersQuery(tribeId)
                .perform()
                .map { it.toJson() }
                .toTypedArray()
    }

}

@Suppress("unused")
@JsName("savePlayerCommandDispatcher")
fun savePlayerCommandDispatcher(jsRepository: dynamic): SavePlayerCommandDispatcher = object : SavePlayerCommandDispatcher {
    override val repository = dataRepository(jsRepository)

    @Suppress("unused")
    @JsName("performCommand")
    fun performCommand(player: Json) = GlobalScope.promise {
        SavePlayerCommand(player.toPlayer())
                .perform()
                .let { it.toJson() }
    }

}

@Suppress("unused")
@JsName("deletePlayerCommandDispatcher")
fun deletePlayerCommandDispatcher(jsRepository: dynamic): DeletePlayerCommandDispatcher = object : DeletePlayerCommandDispatcher {
    override val repository = dataRepository(jsRepository)

    @Suppress("unused")
    @JsName("performCommand")
    fun performCommand(playerId: String) = GlobalScope.promise {
        DeletePlayerCommand(playerId)
                .perform()
                .let { json() }
    }

}