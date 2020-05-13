package com.zegreatrob.coupling.server.action.pairassignmentdocument

import com.zegreatrob.coupling.action.SimpleSuccessfulExecutableAction
import com.zegreatrob.coupling.action.successResult
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.tribe.PairingRule

data class CreatePairCandidateReportsAction(val game: GameSpin) :
    SimpleSuccessfulExecutableAction<CreatePairCandidateReportsActionDispatcher, List<PairCandidateReport>> {
    override val perform = link(CreatePairCandidateReportsActionDispatcher::perform)
}

interface CreatePairCandidateReportsActionDispatcher : PlayerCandidatesFinder {

    val executor: CommandExecutor<CreatePairCandidateReportActionDispatcher>
    private fun performThis(action: CreatePairCandidateReportAction) = executor.execute(action).value

    fun perform(action: CreatePairCandidateReportsAction) = action.createReports()
        .ifEmpty { action.createReportsUsingLongestRule() }
        .successResult()

    private fun CreatePairCandidateReportsAction.createReportsUsingLongestRule() =
        game.createReports(PairingRule.LongestTime)

    private fun CreatePairCandidateReportsAction.createReports() = game.createReports(game.rule)

    private fun GameSpin.createReports(rule: PairingRule) =
        remainingPlayers.mapNotNull { player -> pairCandidateReport(rule, player) }

    private fun GameSpin.pairCandidateReport(rule: PairingRule, player: Player): PairCandidateReport? {
        val candidates = findCandidates(remainingPlayers, rule, player)
        return if (candidates.isNotEmpty() || rule == PairingRule.LongestTime) {
            createReport(player, candidates)
        } else {
            null
        }
    }

    private fun GameSpin.createReport(player: Player, candidates: Array<Player>) =
        performThis(CreatePairCandidateReportAction(player, history, candidates.toList()))
}