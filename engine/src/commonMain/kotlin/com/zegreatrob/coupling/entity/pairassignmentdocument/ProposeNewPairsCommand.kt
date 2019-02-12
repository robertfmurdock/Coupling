package com.zegreatrob.coupling.entity.pairassignmentdocument

import com.zegreatrob.coupling.common.entity.player.Player
import com.zegreatrob.coupling.common.entity.tribe.TribeId
import com.zegreatrob.coupling.entity.CouplingDataRepository
import com.zegreatrob.coupling.entity.tribe.TribeGet

data class ProposeNewPairsCommand(val tribeId: TribeId, val players: List<Player>)
interface ProposeNewPairsCommandDispatcher : TribeIdDataSyntax {

    val actionDispatcher: RunGameActionDispatcher

    suspend fun ProposeNewPairsCommand.perform() = loadData()
            .let { (history, pins, tribe) -> RunGameAction(players, pins, history, tribe!!) }
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
    val tribeRepository: TribeGet

    fun TribeId.getHistoryAsync() = repository.getPairAssignmentsAsync(this)
    fun TribeId.getPinsAsync() = repository.getPinsAsync(this)
    fun TribeId.getTribeAsync() = tribeRepository.getTribeAsync(this)

}
