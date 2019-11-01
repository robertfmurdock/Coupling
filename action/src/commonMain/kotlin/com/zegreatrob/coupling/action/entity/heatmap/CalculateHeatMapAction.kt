package com.zegreatrob.coupling.action.entity.heatmap

import com.zegreatrob.coupling.action.Action
import com.zegreatrob.coupling.action.ActionLoggingSyntax
import com.zegreatrob.coupling.model.pairassignmentdocument.CouplingPair
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.player.Player

data class CalculateHeatMapAction(val players: List<Player>, val history: List<PairAssignmentDocument>, val rotationPeriod: Int) : Action

interface CalculateHeatMapActionDispatcher : ActionLoggingSyntax,
    CalculatePairHeatActionDispatcher {

    fun CalculateHeatMapAction.perform() = log { players.map { player -> heatForEachPair(player) } }

    private fun CalculateHeatMapAction.heatForEachPair(player: Player) =
            players.map { partner -> calculatePairHeat(player, partner) }

    private fun CalculateHeatMapAction.calculatePairHeat(player: Player, alternatePlayer: Player) =
            if (player == alternatePlayer) {
                null
            } else {
                CalculatePairHeatAction(
                    CouplingPair.Double(player, alternatePlayer),
                    history,
                    rotationPeriod
                )
                        .perform()
            }
}