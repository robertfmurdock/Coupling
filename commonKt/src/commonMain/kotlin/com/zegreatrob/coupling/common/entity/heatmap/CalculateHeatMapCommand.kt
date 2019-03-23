package com.zegreatrob.coupling.common.entity.heatmap

import com.zegreatrob.coupling.common.Action
import com.zegreatrob.coupling.common.ActionLoggingSyntax
import com.zegreatrob.coupling.common.entity.pairassignmentdocument.CouplingPair
import com.zegreatrob.coupling.common.entity.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.common.entity.player.Player

data class CalculateHeatMapCommand(val players: List<Player>, val history: List<PairAssignmentDocument>, val rotationPeriod: Int) : Action

interface CalculateHeatMapCommandDispatcher : ActionLoggingSyntax, CalculatePairHeatActionDispatcher {

    fun CalculateHeatMapCommand.perform() = log { players.map { player -> heatForEachPair(player) } }

    private fun CalculateHeatMapCommand.heatForEachPair(player: Player) =
            players.map { partner -> calculatePairHeat(player, partner) }

    private fun CalculateHeatMapCommand.calculatePairHeat(player: Player, alternatePlayer: Player) =
            if (player == alternatePlayer) {
                null
            } else {
                CalculatePairHeatAction(CouplingPair.Double(player, alternatePlayer), history, rotationPeriod)
                        .perform()
            }
}