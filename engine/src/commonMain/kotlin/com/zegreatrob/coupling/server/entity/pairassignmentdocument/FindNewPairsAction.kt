package com.zegreatrob.coupling.server.entity.pairassignmentdocument

import com.zegreatrob.coupling.core.entity.pairassignmentdocument.CouplingPair
import com.zegreatrob.coupling.core.entity.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.core.entity.player.Player
import com.zegreatrob.coupling.core.entity.tribe.PairingRule

data class Game(val history: List<PairAssignmentDocument>, val players: List<Player>, val rule: PairingRule)

data class GameSpin(val history: List<PairAssignmentDocument>, val remainingPlayers: List<Player>, val rule: PairingRule)

private data class Round(val pairs: List<CouplingPair>, val gameSpin: GameSpin)

data class FindNewPairsAction(val game: Game)

interface FindNewPairsActionDispatcher {

    val actionDispatcher: NextPlayerActionDispatcher
    val wheel: Wheel

    fun FindNewPairsAction.perform() = Round(listOf(), game.spinWith(game.players))
            .spinForNextPair()

    private fun Game.spinWith(remainingPlayers: List<Player>) = GameSpin(history, remainingPlayers, rule)

    private fun Round.spinForNextPair(): List<CouplingPair> = getNextPlayer()
            ?.let { playerReport ->
                playerReport.spinForPartner()
                        .let { newPair -> nextRound(newPair) }
                        .spinForNextPair()
            }
            ?: pairs

    private fun Round.getNextPlayer() = if (gameSpin.remainingPlayers.isEmpty()) {
        null
    } else {
        NextPlayerAction(gameSpin)
                .performThis()
    }

    private fun Round.nextRound(newPair: CouplingPair) = Round(
            pairs.plus(newPair),
            gameSpin.copyWithout(newPair)
    )

    private fun GameSpin.copyWithout(newPair: CouplingPair) = copy(
            remainingPlayers = remainingPlayers.minus(newPair.asArray())
    )

    private fun PairCandidateReport.spinForPartner() = if (partners.isEmpty()) {
        CouplingPair.Single(player)
    } else {
        partners.spin()
                .let { partner -> CouplingPair.Double(player, partner) }
    }

    private fun List<Player>.spin() = with(wheel) { toTypedArray().spin() }

    private fun NextPlayerAction.performThis() = with(actionDispatcher) { perform() }


}

