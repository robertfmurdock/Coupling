data class ProposeNewPairsCommand(val tribeId: String, val players: List<Player>)
interface ProposeNewPairsCommandDispatcher : TribeIdDataSyntax {

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
            tribeId.getHistoryAsync(),
            tribeId.getPinsAsync(),
            tribeId.getTribeAsync()
    )

}

interface TribeIdDataSyntax {

    val repository: CouplingDataRepository

    fun String.getHistoryAsync() = repository.getPairAssignmentsAsync(this)
    fun String.getPinsAsync() = repository.getPinsAsync(this)
    fun String.getTribeAsync() = repository.getTribeAsync(this)

}
