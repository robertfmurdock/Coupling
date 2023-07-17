package com.zegreatrob.coupling.server.action.pairassignmentdocument

import com.zegreatrob.coupling.model.map
import com.zegreatrob.coupling.model.party.PairingRule
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.testmints.action.ExecutableActionExecutor
import com.zegreatrob.testmints.action.SimpleExecutableAction
import kotools.types.collection.NotEmptyList

data class CreatePairCandidateReportsAction(val game: GameSpin) :
    SimpleExecutableAction<CreatePairCandidateReportsAction.Dispatcher, NotEmptyList<PairCandidateReport>> {
    override val performFunc = link(Dispatcher::perform)

    interface Dispatcher : PlayerCandidatesFinder {

        val execute: ExecutableActionExecutor<CreatePairCandidateReportAction.Dispatcher>

        fun perform(action: CreatePairCandidateReportsAction) = action.createReports()

        private fun CreatePairCandidateReportsAction.createReportsUsingLongestRule() =
            game.createReports(PairingRule.LongestTime)

        private fun CreatePairCandidateReportsAction.createReports() = game.createReports(game.rule)

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

        private fun GameSpin.createReport(player: Player, candidates: Array<Player>) = execute(
            CreatePairCandidateReportAction(player, history, candidates.toList()),
        )
    }
}
