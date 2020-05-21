package com.zegreatrob.coupling.action.entity.heatmap

import com.zegreatrob.coupling.actionFunc.DispatchSyntax
import com.zegreatrob.coupling.actionFunc.SimpleExecutableAction
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pairassignmentdocument.pairOf
import com.zegreatrob.coupling.model.player.Player

data class CalculateHeatMapAction(
    val players: List<Player>,
    val history: List<PairAssignmentDocument>,
    val rotationPeriod: Int
) : SimpleExecutableAction<CalculateHeatMapActionDispatcher, List<List<Double?>>> {
    override val performFunc = link(CalculateHeatMapActionDispatcher::perform)
}

interface CalculateHeatMapActionDispatcher : CalculatePairHeatActionDispatcher,
    DispatchSyntax {

    fun perform(action: CalculateHeatMapAction) = action.players.map { player ->
        action.heatForEachPair(player)
    }

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
