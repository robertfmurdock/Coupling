package com.zegreatrob.coupling.action.entity.heatmap

import com.zegreatrob.coupling.action.Action
import com.zegreatrob.coupling.action.ActionLoggingSyntax
import com.zegreatrob.coupling.action.SuccessfulExecutableAction
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pairassignmentdocument.pairOf
import com.zegreatrob.coupling.model.player.Player

data class CalculateHeatMapAction(
    val players: List<Player>,
    val history: List<PairAssignmentDocument>,
    val rotationPeriod: Int
) : Action

interface CalculateHeatMapActionDispatcher : ActionLoggingSyntax, CalculatePairHeatActionDispatcher,
    DispatchSyntax {

    fun CalculateHeatMapAction.perform() = log { players.map { player -> heatForEachPair(player) } }

    private fun CalculateHeatMapAction.heatForEachPair(player: Player) = players.map { partner ->
        calculatePairHeat(player, partner)
    }

    private fun CalculateHeatMapAction.calculatePairHeat(player: Player, alternatePlayer: Player) =
        if (player == alternatePlayer) {
            null
        } else {
            execute(
                CalculatePairHeatAction(pairOf(player, alternatePlayer), history, rotationPeriod)
            )
        }
}

interface DispatchSyntax {
    val masterDispatcher: MasterDispatcher get() = MasterDispatcher

    fun <D, R> D.execute(action: SuccessfulExecutableAction<D, R>) = masterDispatcher.invoke(action, this)
}

interface MasterDispatcher {
    operator fun <C : SuccessfulExecutableAction<D, R>, D, R> invoke(command: C, dispatcher: D): R

    companion object : MasterDispatcher {
        override fun <C : SuccessfulExecutableAction<D, R>, D, R> invoke(command: C, dispatcher: D) =
            command.execute(dispatcher).value

    }
}
