package com.zegreatrob.coupling.server.action.pairassignmentdocument

import com.zegreatrob.coupling.model.pairassignmentdocument.NeverPaired
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pairassignmentdocument.TimeResultValue
import com.zegreatrob.coupling.model.party.PairingRule
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.server.action.StubActionExecutor
import com.zegreatrob.coupling.server.action.stubActionExecutor
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.setup
import kotlin.test.Test

class CreatePairCandidateReportsActionTest {

    class WhenThePartyPrefersPairingWithDifferentBadges : CreatePairCandidateReportsActionDispatcher {
        override val execute = stubActionExecutor(CreatePairCandidateReportAction::class)

        @Test
        fun willReturnAllReportsForPlayersWithTheSameBadge() = setup(object {
            val bill = Player(id = "Bill", badge = 1)
            val ted = Player(id = "Ted", badge = 1)
            val amadeus = Player(id = "Mozart", badge = 1)
            val shorty = Player(id = "Napoleon", badge = 1)

            val players = listOf(bill, ted, amadeus, shorty)

            val billReport = PairCandidateReport(bill, emptyList(), TimeResultValue(1))
            val tedReport = PairCandidateReport(ted, emptyList(), TimeResultValue(1))
            val amadeusReport = PairCandidateReport(amadeus, emptyList(), TimeResultValue(1))
            val shortyReport = PairCandidateReport(shorty, emptyList(), TimeResultValue(1))
            val expectedReports = listOf(billReport, tedReport, amadeusReport, shortyReport)

            val history = emptyList<PairAssignmentDocument>()
            val gameSpin = GameSpin(history, players, PairingRule.PreferDifferentBadge)
        }) {
            expectedReports.forEach { report ->
                execute.givenPlayerReturnReport(report, players.without(report.player), history)
            }
        } exercise {
            perform(CreatePairCandidateReportsAction(gameSpin))
        } verify { result ->
            result.assertIsEqualTo(expectedReports)
        }

        @Test
        fun willReturnFilterCandidatesByUnlikeBadge() = setup(object {
            val history = emptyList<PairAssignmentDocument>()
            val bill = Player(id = "Bill", badge = 1)
            val ted = Player(id = "Ted", badge = 1)
            val altAmadeus = Player(id = "Mozart", badge = 2)
            val altShorty = Player(id = "Napoleon", badge = 2)
            val players = listOf(bill, ted, altAmadeus, altShorty)

            val billReport = PairCandidateReport(bill, emptyList(), TimeResultValue(1))
            val tedReport = PairCandidateReport(ted, emptyList(), TimeResultValue(1))
            val amadeusReport = PairCandidateReport(altAmadeus, emptyList(), TimeResultValue(1))
            val shortyReport = PairCandidateReport(altShorty, emptyList(), TimeResultValue(1))
            val expectedReports = listOf(billReport, tedReport, amadeusReport, shortyReport)

            val gameSpin = GameSpin(history, players, PairingRule.PreferDifferentBadge)
        }) {
            execute.run {
                givenPlayerReturnReport(billReport, listOf(altAmadeus, altShorty), history)
                givenPlayerReturnReport(tedReport, listOf(altAmadeus, altShorty), history)
                givenPlayerReturnReport(amadeusReport, listOf(bill, ted), history)
                givenPlayerReturnReport(shortyReport, listOf(bill, ted), history)
            }
        } exercise {
            perform(CreatePairCandidateReportsAction(gameSpin))
        } verify { result ->
            result.assertIsEqualTo(expectedReports)
        }

        @Test
        fun willReturnReportForOnePlayer() = setup(object {
            val history = emptyList<PairAssignmentDocument>()
            val bill = Player(id = "Bill", badge = 1)
            val players = listOf(bill)
            val billReport = PairCandidateReport(bill, emptyList(), TimeResultValue(1))
            val gameSpin = GameSpin(history, players, PairingRule.PreferDifferentBadge)
        }) {
            execute.givenPlayerReturnReport(billReport, emptyList(), history)
        } exercise {
            perform(CreatePairCandidateReportsAction(gameSpin))
        } verify {
            it.assertIsEqualTo(listOf(billReport))
        }
    }

    @Test
    fun whenThePartyPrefersPairingByLongestTime() = setup(object : CreatePairCandidateReportsActionDispatcher {
        override val execute = stubActionExecutor(CreatePairCandidateReportAction::class)
        val history = listOf<PairAssignmentDocument>()
        val bill = Player(id = "Bill", badge = 1)
        val ted = Player(id = "Ted", badge = 1)
        val altAmadeus = Player(id = "Mozart", badge = 2)
        val altShorty = Player(id = "Napoleon", badge = 2)
        val players = listOf(bill, ted, altAmadeus, altShorty)

        val billReport = PairCandidateReport(bill, emptyList(), NeverPaired)
        val tedReport = PairCandidateReport(ted, emptyList(), NeverPaired)
        val amadeusReport = PairCandidateReport(altAmadeus, emptyList(), NeverPaired)
        val shortyReport = PairCandidateReport(altShorty, emptyList(), NeverPaired)
        val expectedReports = listOf(billReport, tedReport, amadeusReport, shortyReport)
    }) {
        execute.run {
            givenPlayerReturnReport(billReport, players.without(bill), history)
            givenPlayerReturnReport(tedReport, players.without(ted), history)
            givenPlayerReturnReport(amadeusReport, players.without(altAmadeus), history)
            givenPlayerReturnReport(shortyReport, players.without(altShorty), history)
        }
    } exercise {
        perform(CreatePairCandidateReportsAction(GameSpin(history, players, PairingRule.LongestTime)))
    } verify {
        it.assertIsEqualTo(expectedReports)
    }

    companion object {

        private fun StubActionExecutor<
            CreatePairCandidateReportActionDispatcher,
            CreatePairCandidateReportAction,
            PairCandidateReport>.givenPlayerReturnReport(
            pairCandidateReport: PairCandidateReport,
            players: List<Player>,
            history: List<PairAssignmentDocument>
        ) = whenever(
            receive = CreatePairCandidateReportAction(pairCandidateReport.player, history, players),
            returnValue = pairCandidateReport
        )

        private fun List<Player>.without(player: Player) = filterNot { it == player }
    }
}
