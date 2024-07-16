package com.zegreatrob.coupling.action.pairassignmentdocument

import com.zegreatrob.coupling.action.CannonProvider
import com.zegreatrob.coupling.model.map
import com.zegreatrob.coupling.model.party.PairingRule
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.testmints.action.annotation.ActionMint
import kotools.types.collection.NotEmptyList

@ActionMint
data class CreatePairCandidateReportListAction(val game: GameSpin) {

    interface Dispatcher<out D> :
        CannonProvider<D>,
        PlayerCandidatesFinder
        where D : CreatePairCandidateReportAction.Dispatcher {

        suspend fun perform(action: CreatePairCandidateReportListAction) = action.createReports()

        private suspend fun CreatePairCandidateReportListAction.createReportsUsingLongestRule() =
            game.createReports(PairingRule.LongestTime)

        private suspend fun CreatePairCandidateReportListAction.createReports(): NotEmptyList<PairCandidateReport> =
            game.createReports(game.rule)

        private suspend fun GameSpin.createReports(rule: PairingRule) =
            remainingPlayers.map { player -> pairCandidateReport(rule, player) }

        private suspend fun GameSpin.pairCandidateReport(rule: PairingRule, player: Player): PairCandidateReport {
            val candidates = findCandidates(remainingPlayers, rule, player)
            return if (candidates.isNotEmpty()) {
                createReport(player, candidates)
            } else {
                createReport(player, findCandidates(remainingPlayers, PairingRule.LongestTime, player))
            }
        }

        private suspend fun GameSpin.createReport(player: Player, candidates: List<Player>) = cannon.fire(
            CreatePairCandidateReportAction(player, history, candidates),
        )
    }
}
