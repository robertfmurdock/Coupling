package com.zegreatrob.coupling.server.action.pairassignmentdocument

import com.zegreatrob.coupling.model.map
import com.zegreatrob.coupling.model.party.PairingRule
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.testmints.action.ExecutableActionExecutor
import com.zegreatrob.testmints.action.annotation.ActionMint
import kotools.types.collection.NotEmptyList

@ActionMint
data class CreatePairCandidateReportListAction(val game: GameSpin) {

    interface Dispatcher<out D> : PlayerCandidatesFinder where D : CreatePairCandidateReportAction.Dispatcher {

        val execute: ExecutableActionExecutor<CreatePairCandidateReportAction.Dispatcher>

        suspend fun perform(action: CreatePairCandidateReportListAction) = action.createReports()

        private fun CreatePairCandidateReportListAction.createReportsUsingLongestRule() =
            game.createReports(PairingRule.LongestTime)

        private fun CreatePairCandidateReportListAction.createReports(): NotEmptyList<PairCandidateReport> =
            game.createReports(game.rule)

        private fun GameSpin.createReports(rule: PairingRule) =
            remainingPlayers.map { player -> pairCandidateReport(rule, player) }

        private fun GameSpin.pairCandidateReport(rule: PairingRule, player: Player): PairCandidateReport {
            val candidates = findCandidates(remainingPlayers, rule, player)
            return if (candidates.isNotEmpty()) {
                createReport(player, candidates)
            } else {
                createReport(player, findCandidates(remainingPlayers, PairingRule.LongestTime, player))
            }
        }

        private fun GameSpin.createReport(player: Player, candidates: List<Player>) = execute(
            CreatePairCandidateReportAction(player, history, candidates),
        )
    }
}
