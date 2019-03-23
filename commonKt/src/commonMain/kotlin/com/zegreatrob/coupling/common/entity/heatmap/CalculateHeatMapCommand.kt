package com.zegreatrob.coupling.common.entity.heatmap

import com.zegreatrob.coupling.common.entity.pairassignmentdocument.CouplingPair
import com.zegreatrob.coupling.common.entity.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.common.entity.player.Player

data class CalculateHeatMapCommand(val players: List<Player>, val history: List<PairAssignmentDocument>, val rotationPeriod: Int)

interface CalculateHeatMapCommandDispatcher : CalculatePairHeatActionDispatcher {
    fun CalculateHeatMapCommand.perform(): List<List<Double?>> {
        return players.map { player ->
            players.map { alternatePlayer ->
                calculatePairHeatAction(player, alternatePlayer)
            }
        }
    }

    private fun CalculateHeatMapCommand.calculatePairHeatAction(player: Player, alternatePlayer: Player) =
            if (player == alternatePlayer) {
                null
            } else {
                CalculatePairHeatAction(CouplingPair.Double(player, alternatePlayer), history, rotationPeriod)
                        .perform()
            }
}