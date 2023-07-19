package com.zegreatrob.coupling.server.action.pairassignmentdocument

import com.zegreatrob.coupling.model.pairassignmentdocument.CouplingPair
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pairassignmentdocument.pairOf
import com.zegreatrob.coupling.model.party.PairingRule
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.server.action.CannonProvider
import com.zegreatrob.testmints.action.annotation.ActionMint
import kotools.types.collection.NotEmptyList
import kotools.types.collection.notEmptyListOf
import kotools.types.collection.toNotEmptyList

@ActionMint
data class FindNewPairsAction(val game: Game) {
    interface Dispatcher<out D> : CannonProvider<D>
        where D : CreatePairCandidateReportListAction.Dispatcher,
              D : NextPlayerAction.Dispatcher<D> {

        val wheel: Wheel

        suspend fun perform(action: FindNewPairsAction): NotEmptyList<CouplingPair> = with(action) {
            val firstGameSpin = game.spinWith(game.players)
            val firstPlayerReport = firstGameSpin.getNextPlayer()
            val newPair = firstPlayerReport.spinForPartner()
            val nextSpin = firstGameSpin.copyWithout(newPair)
            val pairs = notEmptyListOf(newPair)
            return Round(pairs, nextSpin).spinForNextPair()
        }

        private fun Game.spinWith(remainingPlayers: NotEmptyList<Player>) = GameSpin(
            remainingPlayers = remainingPlayers,
            history = history,
            rule = rule,
        )

        private suspend fun Round.spinForNextPair(): NotEmptyList<CouplingPair> = gameSpin
            ?.getNextPlayer()
            ?.let { playerReport ->
                val newPair = playerReport.spinForPartner()
                val updatedPairs = pairs.plus(newPair)
                gameSpin.copyWithout(newPair)
                    ?.let { Round(updatedPairs, it).spinForNextPair() }
                    ?: updatedPairs
            }
            ?: pairs

        private suspend fun GameSpin.getNextPlayer() = cannon.fire(NextPlayerAction(this))

        private fun Pair<Round, CouplingPair>.nextRound(): Round = let { (round, newPair) ->
            Round(
                round.pairs.plus(newPair),
                round.gameSpin?.copyWithout(newPair),
            )
        }

        private fun GameSpin.copyWithout(newPair: CouplingPair): GameSpin? {
            return copy(
                remainingPlayers = remainingPlayers.toList().minus(newPair.toSet()).toNotEmptyList()
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

private fun <E> NotEmptyList<E>.plus(entry: E): NotEmptyList<E> {
    return notEmptyListOf(head, tail = tail?.let { it.toList() + entry }?.toTypedArray() ?: arrayOf(entry))
}

data class Game(val players: NotEmptyList<Player>, val history: List<PairAssignmentDocument>, val rule: PairingRule)

data class GameSpin(
    val remainingPlayers: NotEmptyList<Player>,
    val history: List<PairAssignmentDocument>,
    val rule: PairingRule,
)

private data class Round(val pairs: NotEmptyList<CouplingPair>, val gameSpin: GameSpin?)
