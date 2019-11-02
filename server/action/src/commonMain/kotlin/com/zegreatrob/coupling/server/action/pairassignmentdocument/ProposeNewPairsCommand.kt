package com.zegreatrob.coupling.server.action.pairassignmentdocument

import com.zegreatrob.coupling.action.Action
import com.zegreatrob.coupling.action.ActionLoggingSyntax
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.server.action.tribe.TribeIdGetSyntax

data class ProposeNewPairsCommand(val tribeId: TribeId, val players: List<Player>) : Action
interface ProposeNewPairsCommandDispatcher : ActionLoggingSyntax, TribeIdPinsSyntax, TribeIdGetSyntax, TribeIdHistorySyntax {

    val actionDispatcher: RunGameActionDispatcher

    suspend fun ProposeNewPairsCommand.perform() = logAsync {
        loadData()
                .let { (history, pins, tribe) -> RunGameAction(players, pins, history, tribe!!) }
                .performThis()
    }

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
            tribeId.loadAsync()
    )

}
