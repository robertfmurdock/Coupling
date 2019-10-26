package com.zegreatrob.coupling.common.entity.heatmap

import com.zegreatrob.coupling.common.Action
import com.zegreatrob.coupling.common.ActionLoggingSyntax
import com.zegreatrob.coupling.core.entity.pairassignmentdocument.CouplingPair
import com.zegreatrob.coupling.core.entity.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.core.entity.player.Player

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