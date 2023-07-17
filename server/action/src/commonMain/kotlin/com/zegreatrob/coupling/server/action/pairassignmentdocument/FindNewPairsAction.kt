package com.zegreatrob.coupling.server.action.pairassignmentdocument

import com.zegreatrob.coupling.model.pairassignmentdocument.CouplingPair
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pairassignmentdocument.pairOf
import com.zegreatrob.coupling.model.party.PairingRule
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.testmints.action.ExecutableActionExecutor
import com.zegreatrob.testmints.action.SimpleExecutableAction
import kotools.types.collection.NotEmptyList
import kotools.types.collection.toNotEmptyList

data class FindNewPairsAction(val game: Game) :
    SimpleExecutableAction<FindNewPairsAction.Dispatcher, List<CouplingPair>> {
    override val performFunc = link(Dispatcher::perform)

    interface Dispatcher {

        val execute: ExecutableActionExecutor<NextPlayerAction.Dispatcher>

        val wheel: Wheel

        fun perform(action: FindNewPairsAction) = action.firstRound()
            .spinForNextPair()

        private fun FindNewPairsAction.firstRound() = Round(
            pairs = listOf(),
            gameSpin = game.spinWith(game.players),
        )

        private fun Game.spinWith(remainingPlayers: NotEmptyList<Player>) = GameSpin(
            history = history,
            remainingPlayers = remainingPlayers,
            rule = rule,
        )

        private fun Round.spinForNextPair(): List<CouplingPair> = getNextPlayer()
            ?.let { playerReport ->
                val newPair = playerReport.spinForPartner()
                val updatedPairs = pairs.plus(newPair)
                gameSpin.copyWithout(newPair)
                    ?.let { Round(updatedPairs, it).spinForNextPair() }
                    ?: updatedPairs
            }
            ?: pairs

        private fun Round.getNextPlayer() = execute(NextPlayerAction(gameSpin))

        private fun Pair<Round, CouplingPair>.nextRound(): Round? = let { (round, newPair) ->
            Round(
                round.pairs.plus(newPair),
                round.gameSpin.copyWithout(newPair) ?: return null,
            )
        }

        private fun GameSpin.copyWithout(newPair: CouplingPair): GameSpin? {
            return copy(
                remainingPlayers = remainingPlayers.toList().minus(newPair.asArray().toSet()).toNotEmptyList()
                    .getOrNull()
                    ?: return null,
            )
        }

        private fun PairCandidateReport.spinForPartner() = if (partners.isEmpty()) {
            pairOf(player)
        } else {
            pairOf(player, partners.nextPlayer())
        }

        private fun List<Player>.nextPlayer() = with(wheel) { toTypedArray().spin() }
    }
}

data class Game(val history: List<PairAssignmentDocument>, val players: NotEmptyList<Player>, val rule: PairingRule)

data class GameSpin(
    val history: List<PairAssignmentDocument>,
    val remainingPlayers: NotEmptyList<Player>,
    val rule: PairingRule,
)

private data class Round(val pairs: List<CouplingPair>, val gameSpin: GameSpin)
