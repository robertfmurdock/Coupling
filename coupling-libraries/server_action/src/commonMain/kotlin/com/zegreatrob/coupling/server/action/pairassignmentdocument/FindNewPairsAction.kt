package com.zegreatrob.coupling.server.action.pairassignmentdocument

import com.zegreatrob.coupling.model.pairassignmentdocument.CouplingPair
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pairassignmentdocument.pairOf
import com.zegreatrob.coupling.model.party.PairingRule
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.testmints.action.ExecutableActionExecutor
import com.zegreatrob.testmints.action.SimpleExecutableAction

data class FindNewPairsAction(val game: Game) :
    SimpleExecutableAction<FindNewPairsActionDispatcher, List<CouplingPair>> {
    override val performFunc = link(FindNewPairsActionDispatcher::perform)
}

data class Game(val history: List<PairAssignmentDocument>, val players: List<Player>, val rule: PairingRule)

data class GameSpin(
    val history: List<PairAssignmentDocument>,
    val remainingPlayers: List<Player>,
    val rule: PairingRule,
)

private data class Round(val pairs: List<CouplingPair>, val gameSpin: GameSpin)

interface FindNewPairsActionDispatcher {

    val execute: ExecutableActionExecutor<NextPlayerActionDispatcher>

    val wheel: Wheel

    fun perform(action: FindNewPairsAction) = action.round()
        .spinForNextPair()

    private fun FindNewPairsAction.round() = Round(listOf(), game.spinWith(game.players))

    private fun Game.spinWith(remainingPlayers: List<Player>) = GameSpin(history, remainingPlayers, rule)

    private fun Round.spinForNextPair(): List<CouplingPair> = getNextPlayer()
        ?.let { playerReport ->
            (this to playerReport.spinForPartner())
                .nextRound()
                .spinForNextPair()
        }
        ?: pairs

    private fun Round.getNextPlayer() = if (gameSpin.remainingPlayers.isEmpty()) {
        null
    } else {
        execute(NextPlayerAction(gameSpin))
    }

    private fun Pair<Round, CouplingPair>.nextRound() = let { (round, newPair) ->
        Round(
            round.pairs.plus(newPair),
            round.gameSpin.copyWithout(newPair),
        )
    }

    private fun GameSpin.copyWithout(newPair: CouplingPair) = copy(
        remainingPlayers = remainingPlayers.minus(newPair.asArray()),
    )

    private fun PairCandidateReport.spinForPartner() = if (partners.isEmpty()) {
        pairOf(player)
    } else {
        pairOf(player, partners.nextPlayer())
    }

    private fun List<Player>.nextPlayer() = with(wheel) { toTypedArray().spin() }
}
