package com.zegreatrob.coupling.client.components.stats

import com.benasher44.uuid.uuid4
import com.soywiz.klock.DateTime
import com.zegreatrob.coupling.action.stats.ComposeStatisticsAction
import com.zegreatrob.coupling.action.stats.StatisticsQuery
import com.zegreatrob.coupling.action.stats.heatmap.CalculateHeatMapAction
import com.zegreatrob.coupling.model.pairassignmentdocument.CouplingPair
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocumentId
import com.zegreatrob.coupling.model.pairassignmentdocument.PinnedCouplingPair
import com.zegreatrob.coupling.model.pairassignmentdocument.pairOf
import com.zegreatrob.coupling.model.pairassignmentdocument.withPins
import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.testreact.external.testinglibrary.react.render
import com.zegreatrob.coupling.testreact.external.testinglibrary.react.screen
import com.zegreatrob.coupling.testreact.external.testinglibrary.react.within
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.minreact.create
import com.zegreatrob.testmints.setup
import org.w3c.dom.HTMLElement
import org.w3c.dom.asList
import react.router.MemoryRouter
import kotlin.js.json
import kotlin.test.Test

class PartyStatisticsTest :
    CalculateHeatMapAction.Dispatcher,
    ComposeStatisticsAction.Dispatcher {

    @Test
    fun willShowPairings() = setup(object {
        val players = listOf(
            Player("harry", name = "Harry", avatarType = null),
            Player("larry", name = "Larry", avatarType = null),
            Player("curry", name = "Curly", avatarType = null),
            Player("moe", name = "Moe", avatarType = null),
        )
        val party = Party(PartyId("1"), name = "Mathematica")
        val history = listOf(
            PairAssignmentDocument(
                id = PairAssignmentDocumentId("${uuid4()}"),
                date = DateTime.now(),
                pairs = listOf<CouplingPair>(
                    pairOf(players[0], players[1]),
                    pairOf(players[2], players[3]),
                ).withNoPins(),
            ),
        )
        val report = perform(ComposeStatisticsAction(party, players, history))
    }) exercise {
        render(
            PartyStatistics(StatisticsQuery.Results(party, players, history, report, emptyList())).create(),
            json("wrapper" to MemoryRouter),
        )
    } verify { result ->
        result.baseElement.querySelectorAll("[data-pair-report]")
            .asList()
            .map { it as HTMLElement }
            .map { it.getAttribute("data-pair-report") }
            .assertIsEqualTo(
                listOf(
                    "Harry-Curly",
                    "Harry-Moe",
                    "Larry-Curly",
                    "Larry-Moe",
                    "Harry-Larry",
                    "Curly-Moe",
                ),
            )
        result.baseElement.querySelectorAll("[data-time-since-last-pair]")
            .asList()
            .map { it as HTMLElement }
            .map { it.textContent }
            .assertIsEqualTo(
                listOf(
                    "Never Paired",
                    "Never Paired",
                    "Never Paired",
                    "Never Paired",
                    "0",
                    "0",
                ),
            )
    }

    @Test
    fun sendsPlayerHeatDataToSubComponent() = setup(object {
        val players = listOf(
            Player("harry", name = "Harry", avatarType = null),
            Player("larry", name = "Larry", avatarType = null),
            Player("curry", name = "Curly", avatarType = null),
            Player("moe", name = "Moe", avatarType = null),
        )
        val history = listOf(
            PairAssignmentDocument(
                id = PairAssignmentDocumentId("${uuid4()}"),
                date = DateTime.now(),
                pairs = listOf(
                    pairOf(players[0], players[1]),
                    pairOf(players[2], players[3]),
                ).withNoPins(),
            ),
        )
        val party = Party(PartyId("2"), name = "Mathematica")
        val report = perform(ComposeStatisticsAction(party, players, history))
        val heatmapData = perform(CalculateHeatMapAction(players, history, report.spinsUntilFullRotation))
    }) exercise {
        render(
            PartyStatistics(StatisticsQuery.Results(party, players, history, report, heatmapData)).create(),
            json("wrapper" to MemoryRouter),
        )
    } verify { wrapper ->
        wrapper.baseElement.querySelector("[data-heatmap]")
            .let { it as HTMLElement }
            .getAttribute("data-heatmap")
            .assertIsEqualTo(
                listOf(
                    listOf(null, 1, 0, 0),
                    listOf(1, null, 0, 0),
                    listOf(0, 0, null, 1),
                    listOf(0, 0, 1, null),
                ).joinToString(",") {
                    "[${it.joinToString(",")}]"
                },
            )
    }

    @Test
    fun willShowBasicStatisticsOnSubComponent() = setup(object {
        val players = listOf(
            Player("harry", name = "Harry", avatarType = null),
            Player("larry", name = "Larry", avatarType = null),
            Player("curry", name = "Curly", avatarType = null),
            Player("moe", name = "Moe", avatarType = null),
        )
        val party = Party(
            PartyId("2"),
            name = "Mathematica",
        )
        val report = perform(ComposeStatisticsAction(party, players, emptyList()))
    }) exercise {
        render(
            PartyStatistics(StatisticsQuery.Results(party, players, emptyList(), report, emptyList())).create(),
            json("wrapper" to MemoryRouter),
        )
    } verify {
        within(screen.getByText("Spins Until Full Rotation:").parentElement)
            .getByText("3")
        within(screen.getByText("Number of Active Players:").parentElement)
            .getByText("4")
    }

    @Test
    fun willShowTheMedianSpinTimeOnSubComponent() = setup(object {
        val players = listOf(
            Player("harry", name = "Harry", avatarType = null),
            Player("larry", name = "Larry", avatarType = null),
            Player("curry", name = "Curly", avatarType = null),
            Player("moe", name = "Moe", avatarType = null),
        )
        val party = Party(PartyId("2"), name = "Mathematica")
        val history = listOf(
            PairAssignmentDocument(
                id = PairAssignmentDocumentId("${uuid4()}"),
                date = DateTime(2017, 3, 14),
                pairs = listOf(
                    pairOf(players[0], players[1]),
                    pairOf(players[2], players[3]),
                ).withNoPins(),
            ),
            PairAssignmentDocument(
                id = PairAssignmentDocumentId("${uuid4()}"),
                date = DateTime(2017, 3, 12),
                pairs = listOf(
                    pairOf(players[0], players[1]),
                    pairOf(players[2], players[3]),
                ).withNoPins(),
            ),
        )
        val report = perform(ComposeStatisticsAction(party, players, history))
    }) exercise {
        render(
            PartyStatistics(StatisticsQuery.Results(party, players, history, report, emptyList())).create(),
            json("wrapper" to MemoryRouter),
        )
    } verify {
        within(screen.getByText("Median Spin Duration:").parentElement)
            .getByText("2 days")
    }
}

private fun List<CouplingPair>.withNoPins() = map { pair -> pair.toPinnedPair() }

private fun CouplingPair.toPinnedPair() = PinnedCouplingPair(toPinnedPlayers())

private fun CouplingPair.toPinnedPlayers() = asArray().map { player -> player.withPins(emptyList()) }
