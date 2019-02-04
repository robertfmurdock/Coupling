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
    it.asArray()
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
    override val repository: CouplingDataRepository = dataRepository(jsRepository)
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
    override val repository: CouplingDataRepository
        get() = dataRepository(jsRepository)

    @Suppress("unused")
    @JsName("performQuery")
    fun performQuery(tribeId: String) = GlobalScope.promise {
        PlayersQuery(tribeId)
                .perform()
                .map { it.toJson() }
                .toTypedArray()
    }

}