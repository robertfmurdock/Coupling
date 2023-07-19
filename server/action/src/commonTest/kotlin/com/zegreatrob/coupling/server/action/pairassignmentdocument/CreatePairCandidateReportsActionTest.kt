package com.zegreatrob.coupling.server.action.pairassignmentdocument

import com.zegreatrob.coupling.model.pairassignmentdocument.NeverPaired
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pairassignmentdocument.TimeResultValue
import com.zegreatrob.coupling.model.party.PairingRule
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.server.action.StubActionExecutor
import com.zegreatrob.coupling.server.action.stubActionExecutor
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.asyncSetup
import kotools.types.collection.NotEmptyList
import kotools.types.collection.notEmptyListOf
import kotlin.test.Test

class CreatePairCandidateReportsActionTest {

    class WhenThePartyPrefersPairingWithDifferentBadges :
        CreatePairCandidateReportListAction.Dispatcher<CreatePairCandidateReportAction.Dispatcher> {
        override val execute = stubActionExecutor(CreatePairCandidateReportAction::class)

        @Test
        fun willReturnAllReportsForPlayersWithTheSameBadge() = asyncSetup(object {
            val bill = Player(id = "Bill", badge = 1, avatarType = null)
            val ted = Player(id = "Ted", badge = 1, avatarType = null)
            val amadeus = Player(id = "Mozart", badge = 1, avatarType = null)
            val shorty = Player(id = "Napoleon", badge = 1, avatarType = null)

            val players = notEmptyListOf(bill, ted, amadeus, shorty)

            val billReport = PairCandidateReport(bill, emptyList(), TimeResultValue(1))
            val tedReport = PairCandidateReport(ted, emptyList(), TimeResultValue(1))
            val amadeusReport = PairCandidateReport(amadeus, emptyList(), TimeResultValue(1))
            val shortyReport = PairCandidateReport(shorty, emptyList(), TimeResultValue(1))
            val expectedReports = notEmptyListOf(billReport, tedReport, amadeusReport, shortyReport)

            val history = emptyList<PairAssignmentDocument>()
            val gameSpin = GameSpin(players, history, PairingRule.PreferDifferentBadge)
        }) {
            expectedReports.toList().forEach { report ->
                execute.givenPlayerReturnReport(report, players.without(report.player), history)
            }
        } exercise {
            perform(CreatePairCandidateReportListAction(gameSpin))
        } verify { result ->
            result.assertIsEqualTo(expectedReports)
        }

        @Test
        fun willReturnFilterCandidatesByUnlikeBadge() = asyncSetup(object {
            val history = emptyList<PairAssignmentDocument>()
            val bill = Player(id = "Bill", badge = 1, avatarType = null)
            val ted = Player(id = "Ted", badge = 1, avatarType = null)
            val altAmadeus = Player(id = "Mozart", badge = 2, avatarType = null)
            val altShorty = Player(id = "Napoleon", badge = 2, avatarType = null)
            val players = notEmptyListOf(bill, ted, altAmadeus, altShorty)

            val billReport = PairCandidateReport(bill, emptyList(), TimeResultValue(1))
            val tedReport = PairCandidateReport(ted, emptyList(), TimeResultValue(1))
            val amadeusReport = PairCandidateReport(altAmadeus, emptyList(), TimeResultValue(1))
            val shortyReport = PairCandidateReport(altShorty, emptyList(), TimeResultValue(1))
            val expectedReports = notEmptyListOf(billReport, tedReport, amadeusReport, shortyReport)

            val gameSpin = GameSpin(players, history, PairingRule.PreferDifferentBadge)
        }) {
            execute.run {
                givenPlayerReturnReport(billReport, listOf(altAmadeus, altShorty), history)
                givenPlayerReturnReport(tedReport, listOf(altAmadeus, altShorty), history)
                givenPlayerReturnReport(amadeusReport, listOf(bill, ted), history)
                givenPlayerReturnReport(shortyReport, listOf(bill, ted), history)
            }
        } exercise {
            perform(CreatePairCandidateReportListAction(gameSpin))
        } verify { result ->
            result.assertIsEqualTo(expectedReports)
        }

        @Test
        fun willReturnReportForOnePlayer() = asyncSetup(object {
            val history = emptyList<PairAssignmentDocument>()
            val bill = Player(id = "Bill", badge = 1, avatarType = null)
            val players = notEmptyListOf(bill)
            val billReport = PairCandidateReport(bill, emptyList(), TimeResultValue(1))
            val gameSpin = GameSpin(players, history, PairingRule.PreferDifferentBadge)
        }) {
            execute.givenPlayerReturnReport(billReport, emptyList(), history)
        } exercise {
            perform(CreatePairCandidateReportListAction(gameSpin))
        } verify {
            it.assertIsEqualTo(notEmptyListOf(billReport))
        }
    }

    @Test
    fun whenThePartyPrefersPairingByLongestTime() = asyncSetup(object :
        CreatePairCandidateReportListAction.Dispatcher<CreatePairCandidateReportAction.Dispatcher> {
        override val execute = stubActionExecutor(CreatePairCandidateReportAction::class)
        val history = listOf<PairAssignmentDocument>()
        val bill = Player(id = "Bill", badge = 1, avatarType = null)
        val ted = Player(id = "Ted", badge = 1, avatarType = null)
        val altAmadeus = Player(id = "Mozart", badge = 2, avatarType = null)
        val altShorty = Player(id = "Napoleon", badge = 2, avatarType = null)
        val players = notEmptyListOf(bill, ted, altAmadeus, altShorty)

        val billReport = PairCandidateReport(bill, emptyList(), NeverPaired)
        val tedReport = PairCandidateReport(ted, emptyList(), NeverPaired)
        val amadeusReport = PairCandidateReport(altAmadeus, emptyList(), NeverPaired)
        val shortyReport = PairCandidateReport(altShorty, emptyList(), NeverPaired)
        val expectedReports = notEmptyListOf(billReport, tedReport, amadeusReport, shortyReport)
    }) {
        execute.run {
            givenPlayerReturnReport(billReport, players.without(bill), history)
            givenPlayerReturnReport(tedReport, players.without(ted), history)
            givenPlayerReturnReport(amadeusReport, players.without(altAmadeus), history)
            givenPlayerReturnReport(shortyReport, players.without(altShorty), history)
        }
    } exercise {
        perform(CreatePairCandidateReportListAction(GameSpin(players, history, PairingRule.LongestTime)))
    } verify {
        it.assertIsEqualTo(expectedReports)
    }

    companion object {

        private fun StubActionExecutor<
            CreatePairCandidateReportAction.Dispatcher,
            CreatePairCandidateReportAction,
            PairCandidateReport,
            >.givenPlayerReturnReport(
            pairCandidateReport: PairCandidateReport,
            players: List<Player>,
            history: List<PairAssignmentDocument>,
        ) = whenever(
            receive = CreatePairCandidateReportAction(pairCandidateReport.player, history, players),
            returnValue = pairCandidateReport,
        )

        private fun NotEmptyList<Player>.without(player: Player) = toList().filterNot { it == player }
    }
}
