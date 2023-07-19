package com.zegreatrob.coupling.server.action.pairassignmentdocument

import com.zegreatrob.coupling.model.map
import com.zegreatrob.coupling.model.party.PairingRule
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.testmints.action.ExecutableActionExecutor
import com.zegreatrob.testmints.action.async.SimpleSuspendAction
import kotools.types.collection.NotEmptyList

data class CreatePairCandidateReportListAction(val game: GameSpin) :
    SimpleSuspendAction<CreatePairCandidateReportListAction.Dispatcher, NotEmptyList<PairCandidateReport>> {
    override val performFunc = link(Dispatcher::perform)

    interface Dispatcher : PlayerCandidatesFinder {

        val execute: ExecutableActionExecutor<CreatePairCandidateReportAction.Dispatcher>

        fun perform(action: CreatePairCandidateReportListAction) = action.createReports()

        private fun CreatePairCandidateReportListAction.createReportsUsingLongestRule() =
            game.createReports(PairingRule.LongestTime)

        private fun CreatePairCandidateReportListAction.createReports() = game.createReports(game.rule)

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
