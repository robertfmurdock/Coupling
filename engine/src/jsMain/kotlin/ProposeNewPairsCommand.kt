import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.promise
import kotlin.js.Json
import kotlin.js.json

data class ProposeNewPairsCommand(val tribeId: String, val players: List<Player>)
interface ProposeNewPairsCommandDispatcher : TribeIdDataSyntax {

    @Suppress("unused")
    @JsName("performProposeNewPairsCommand")
    fun performProposeNewPairsCommand(tribeId: String, players: Array<Json>) =
            GlobalScope.promise {
                ProposeNewPairsCommand(tribeId, players.map { it.toPlayer() })
                        .perform()
                        .let {
                            json(
                                    "date" to it.date,
                                    "pairs" to toJs(it),
                                    "tribe" to it.tribeId
                            )
                        }
            }

    private fun toJs(it: PairAssignmentDocument): Array<Array<Json>> {
        return it.pairs.map {
            it.asArray()
                    .map { player ->
                        player.toJson()
                    }
                    .toTypedArray()
        }
                .toTypedArray()
    }

    val actionDispatcher: RunGameActionDispatcher

    suspend fun ProposeNewPairsCommand.perform() = loadData()
            .let { (history, pins, tribe) -> RunGameAction(players, pins, history, tribe) }
            .performThis()

    private fun RunGameAction.performThis() = with(actionDispatcher) { perform() }

    private suspend fun ProposeNewPairsCommand.loadData() = dataDeferred()
            .let { (historyDeferred, pinsDeferred, tribeDeferred) ->
                Triple(
                        historyDeferred.await(),
                        pinsDeferred.await(),
                        tribeDeferred.await()
                )
            }

    private fun ProposeNewPairsCommand.dataDeferred() = Triple(
            tribeId.getHistory(),
            tribeId.getPins(),
            tribeId.getTribe()
    )

}

interface TribeIdDataSyntax {

    val repository: CouplingDataRepository

    fun String.getHistory() = repository.getHistory(this)
    fun String.getPins() = repository.getPins(this)
    fun String.getTribe() = repository.getTribe(this)

}
