package com.zegreatrob.coupling.server.action.pairassignmentdocument

import com.zegreatrob.coupling.action.pairassignmentdocument.CreatePairCandidateReportAction
import com.zegreatrob.coupling.action.pairassignmentdocument.CreatePairCandidateReportListAction
import com.zegreatrob.coupling.action.pairassignmentdocument.GameSpin
import com.zegreatrob.coupling.action.pairassignmentdocument.PairCandidateReport
import com.zegreatrob.coupling.action.pairassignmentdocument.let
import com.zegreatrob.coupling.model.forEach
import com.zegreatrob.coupling.model.pairassignmentdocument.NeverPaired
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pairassignmentdocument.TimeResultValue
import com.zegreatrob.coupling.model.party.PairingRule
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.stubmodel.stubPlayer
import com.zegreatrob.coupling.testaction.StubCannon
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.ScopeMint
import com.zegreatrob.testmints.async.asyncSetup
import kotools.types.collection.NotEmptyList
import kotools.types.collection.notEmptyListOf
import kotlin.test.Test

class CreatePairCandidateReportsActionTest {

    interface CreatePairCandidateReportsActionTestDispatcher :
        CreatePairCandidateReportListAction.Dispatcher<CreatePairCandidateReportsActionTestDispatcher>,
        CreatePairCandidateReportAction.Dispatcher

    class WhenThePartyPrefersPairingWithDifferentBadges {

        @Test
        fun willReturnAllReportsForPlayersWithTheSameBadge() = asyncSetup(object :
            ScopeMint(),
            CreatePairCandidateReportsActionTestDispatcher {
            val bill = stubPlayer().copy(name = "Bill", badge = 1)
            val ted = stubPlayer().copy(name = "Ted", badge = 1)
            val amadeus = stubPlayer().copy(name = "Mozart", badge = 1)
            val shorty = stubPlayer().copy(name = "Napoleon", badge = 1)

            val players = notEmptyListOf(bill, ted, amadeus, shorty)

            val billReport = PairCandidateReport(bill, emptyList(), TimeResultValue(1))
            val tedReport = PairCandidateReport(ted, emptyList(), TimeResultValue(1))
            val amadeusReport = PairCandidateReport(amadeus, emptyList(), TimeResultValue(1))
            val shortyReport = PairCandidateReport(shorty, emptyList(), TimeResultValue(1))
            val expectedReports = notEmptyListOf(billReport, tedReport, amadeusReport, shortyReport)

            val history = emptyList<PairAssignmentDocument>()
            val gameSpin = GameSpin(players, history, PairingRule.PreferDifferentBadge)
            override val cannon = StubCannon.Synchronous<CreatePairCandidateReportsActionTestDispatcher>().apply {
                expectedReports.forEach { report ->
                    CreatePairCandidateReportAction(report.player, history, players.without(report.player))
                        .let(this::given)
                        .thenReturn(report)
                }
            }

//            val actionChannel = Channel<SuspendAction<CreatePairCandidateReportsActionTestDispatcher, *>>()
//                actionChannel,
//                exerciseScope.produce { expectedReports.forEach { report -> actionChannel.receive(); send(report) } },
        }) exercise {
            perform(CreatePairCandidateReportListAction(gameSpin))
        } verify { result ->
            result.assertIsEqualTo(expectedReports)
        }

        @Test
        fun willReturnFilterCandidatesByUnlikeBadge() = asyncSetup(object :
            ScopeMint(),
            CreatePairCandidateReportsActionTestDispatcher {
            val history = emptyList<PairAssignmentDocument>()
            val bill = stubPlayer().copy(name = "Bill", badge = 1)
            val ted = stubPlayer().copy(name = "Ted", badge = 1)
            val altAmadeus = stubPlayer().copy(name = "Mozart", badge = 2)
            val altShorty = stubPlayer().copy(name = "Napoleon", badge = 2)
            val players = notEmptyListOf(bill, ted, altAmadeus, altShorty)

            val billReport = PairCandidateReport(bill, emptyList(), TimeResultValue(1))
            val tedReport = PairCandidateReport(ted, emptyList(), TimeResultValue(1))
            val amadeusReport = PairCandidateReport(altAmadeus, emptyList(), TimeResultValue(1))
            val shortyReport = PairCandidateReport(altShorty, emptyList(), TimeResultValue(1))
            val expectedReports = notEmptyListOf(billReport, tedReport, amadeusReport, shortyReport)

            val gameSpin = GameSpin(players, history, PairingRule.PreferDifferentBadge)
            override val cannon = StubCannon.Synchronous<CreatePairCandidateReportsActionTestDispatcher>().apply {
                CreatePairCandidateReportAction(bill, history, listOf(altAmadeus, altShorty))
                    .let(::given).thenReturn(billReport)
                CreatePairCandidateReportAction(ted, history, listOf(altAmadeus, altShorty))
                    .let(::given).thenReturn(tedReport)
                CreatePairCandidateReportAction(altAmadeus, history, listOf(bill, ted))
                    .let(::given).thenReturn(amadeusReport)
                CreatePairCandidateReportAction(altShorty, history, listOf(bill, ted))
                    .let(::given).thenReturn(shortyReport)
            }
        }) exercise {
            perform(CreatePairCandidateReportListAction(gameSpin))
        } verify { result ->
            result.assertIsEqualTo(expectedReports)
        }

        @Test
        fun willReturnReportForOnePlayer() = asyncSetup(object :
            ScopeMint(),
            CreatePairCandidateReportsActionTestDispatcher {
            val history = emptyList<PairAssignmentDocument>()
            val bill = stubPlayer().copy(name = "Bill", badge = 1)
            val players = notEmptyListOf(bill)
            val billReport = PairCandidateReport(bill, emptyList(), TimeResultValue(1))
            val gameSpin = GameSpin(players, history, PairingRule.PreferDifferentBadge)
            override val cannon = StubCannon.Synchronous<CreatePairCandidateReportsActionTestDispatcher>().apply {
                CreatePairCandidateReportAction(bill, history, emptyList())
                    .let(::given).thenReturn(billReport)
            }
        }) exercise {
            perform(CreatePairCandidateReportListAction(gameSpin))
        } verify {
            it.assertIsEqualTo(notEmptyListOf(billReport))
        }
    }

    @Test
    fun whenThePartyPrefersPairingByLongestTime() = asyncSetup(object :
        ScopeMint(),
        CreatePairCandidateReportsActionTestDispatcher {
        val history = listOf<PairAssignmentDocument>()
        val bill = stubPlayer().copy(name = "Bill", badge = 1)
        val ted = stubPlayer().copy(name = "Ted", badge = 1)
        val altAmadeus = stubPlayer().copy(name = "Mozart", badge = 2)
        val altShorty = stubPlayer().copy(name = "Napoleon", badge = 2)
        val players = notEmptyListOf(bill, ted, altAmadeus, altShorty)

        val billReport = PairCandidateReport(bill, emptyList(), NeverPaired)
        val tedReport = PairCandidateReport(ted, emptyList(), NeverPaired)
        val amadeusReport = PairCandidateReport(altAmadeus, emptyList(), NeverPaired)
        val shortyReport = PairCandidateReport(altShorty, emptyList(), NeverPaired)
        val expectedReports = notEmptyListOf(billReport, tedReport, amadeusReport, shortyReport)
        override val cannon = StubCannon.Synchronous<CreatePairCandidateReportsActionTestDispatcher>().apply {
            CreatePairCandidateReportAction(bill, history, players.without(bill))
                .let(::given).thenReturn(billReport)
            CreatePairCandidateReportAction(ted, history, players.without(ted))
                .let(::given).thenReturn(tedReport)
            CreatePairCandidateReportAction(altAmadeus, history, players.without(altAmadeus))
                .let(::given).thenReturn(amadeusReport)
            CreatePairCandidateReportAction(altShorty, history, players.without(altShorty))
                .let(::given).thenReturn(shortyReport)
        }
    }) exercise {
        perform(CreatePairCandidateReportListAction(GameSpin(players, history, PairingRule.LongestTime)))
    } verify {
        it.assertIsEqualTo(expectedReports)
    }

    companion object {
        private fun NotEmptyList<Player>.without(player: Player) = toList().filterNot { it == player }
    }
}
