package com.zegreatrob.coupling.server.action.pairassignmentdocument

import com.zegreatrob.coupling.model.forEach
import com.zegreatrob.coupling.model.map
import com.zegreatrob.coupling.model.pairassignmentdocument.NeverPaired
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pairassignmentdocument.TimeResultValue
import com.zegreatrob.coupling.model.party.PairingRule
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.testaction.StubCannon
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.ScopeMint
import com.zegreatrob.testmints.async.asyncSetup
import kotlinx.coroutines.channels.produce
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
            val receivedActions = mutableListOf<Any?>()
            override val cannon = StubCannon<CreatePairCandidateReportsActionTestDispatcher>(
                receivedActions,
                exerciseScope.produce { expectedReports.forEach { report -> send(report) } },
            )
        }) exercise {
            perform(CreatePairCandidateReportListAction(gameSpin))
        } verify { result ->
            result.assertIsEqualTo(expectedReports)
            receivedActions.assertIsEqualTo(
                expectedReports.map { CreatePairCandidateReportAction(it.player, history, players.without(it.player)) }
                    .toList(),
            )
        }

        @Test
        fun willReturnFilterCandidatesByUnlikeBadge() = asyncSetup(object :
            ScopeMint(),
            CreatePairCandidateReportsActionTestDispatcher {
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
            val receivedActions = mutableListOf<Any?>()
            override val cannon = StubCannon<CreatePairCandidateReportsActionTestDispatcher>(
                receivedActions,
                exerciseScope.produce {
                    expectedReports.forEach { report -> send(report) }
                },
            )
        }) exercise {
            perform(CreatePairCandidateReportListAction(gameSpin))
        } verify { result ->
            result.assertIsEqualTo(expectedReports)
            receivedActions.assertIsEqualTo(
                listOf(
                    CreatePairCandidateReportAction(bill, history, listOf(altAmadeus, altShorty)),
                    CreatePairCandidateReportAction(ted, history, listOf(altAmadeus, altShorty)),
                    CreatePairCandidateReportAction(altAmadeus, history, listOf(bill, ted)),
                    CreatePairCandidateReportAction(altShorty, history, listOf(bill, ted)),
                ),
            )
        }

        @Test
        fun willReturnReportForOnePlayer() = asyncSetup(object :
            ScopeMint(),
            CreatePairCandidateReportsActionTestDispatcher {
            val history = emptyList<PairAssignmentDocument>()
            val bill = Player(id = "Bill", badge = 1, avatarType = null)
            val players = notEmptyListOf(bill)
            val billReport = PairCandidateReport(bill, emptyList(), TimeResultValue(1))
            val gameSpin = GameSpin(players, history, PairingRule.PreferDifferentBadge)
            val receivedActions = mutableListOf<Any?>()
            override val cannon = StubCannon<CreatePairCandidateReportsActionTestDispatcher>(
                receivedActions,
                exerciseScope.produce { send(billReport) },
            )
        }) exercise {
            perform(CreatePairCandidateReportListAction(gameSpin))
        } verify {
            it.assertIsEqualTo(notEmptyListOf(billReport))
            receivedActions.assertIsEqualTo(listOf(CreatePairCandidateReportAction(bill, history, emptyList())))
        }
    }

    @Test
    fun whenThePartyPrefersPairingByLongestTime() = asyncSetup(object :
        ScopeMint(),
        CreatePairCandidateReportsActionTestDispatcher {
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
        val receivedActions = mutableListOf<Any?>()
        override val cannon = StubCannon<CreatePairCandidateReportsActionTestDispatcher>(
            receivedActions,
            exerciseScope.produce { expectedReports.forEach { report -> send(report) } },
        )
    }) exercise {
        perform(CreatePairCandidateReportListAction(GameSpin(players, history, PairingRule.LongestTime)))
    } verify {
        it.assertIsEqualTo(expectedReports)
        receivedActions.assertIsEqualTo(
            listOf(
                CreatePairCandidateReportAction(bill, history, players.without(bill)),
                CreatePairCandidateReportAction(ted, history, players.without(ted)),
                CreatePairCandidateReportAction(altAmadeus, history, players.without(altAmadeus)),
                CreatePairCandidateReportAction(altShorty, history, players.without(altShorty)),
            ),
        )
    }

    companion object {
        private fun NotEmptyList<Player>.without(player: Player) = toList().filterNot { it == player }
    }
}
