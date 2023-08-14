package com.zegreatrob.coupling.client.components.stats

import com.benasher44.uuid.uuid4
import com.zegreatrob.coupling.action.stats.ComposeStatisticsAction
import com.zegreatrob.coupling.action.stats.StatisticsQuery
import com.zegreatrob.coupling.action.stats.heatmap.CalculateHeatMapAction
import com.zegreatrob.coupling.model.PlayerPair
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.pairassignmentdocument.CouplingPair
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocumentId
import com.zegreatrob.coupling.model.pairassignmentdocument.pairOf
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.model.party.PartyElement
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.withNoPins
import com.zegreatrob.coupling.stubmodel.stubPartyId
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.setup
import com.zegreatrob.wrapper.testinglibrary.react.TestingLibraryReact.render
import com.zegreatrob.wrapper.testinglibrary.react.TestingLibraryReact.screen
import com.zegreatrob.wrapper.testinglibrary.react.TestingLibraryReact.within
import js.core.jso
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotools.types.collection.notEmptyListOf
import org.w3c.dom.HTMLElement
import org.w3c.dom.asList
import react.router.MemoryRouter
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
        val party = PartyDetails(PartyId("1"), name = "Mathematica")
        val history = listOf(
            PairAssignmentDocument(
                id = PairAssignmentDocumentId("${uuid4()}"),
                date = Clock.System.now(),
                pairs = notEmptyListOf<CouplingPair>(
                    pairOf(players[0], players[1]),
                    pairOf(players[2], players[3]),
                ).withNoPins(),
            ),
        )
        val report = perform(ComposeStatisticsAction(party, players, history))
    }) exercise {
        render(jso { wrapper = MemoryRouter }) {
            PartyStatistics(
                StatisticsQuery.Results(
                    party,
                    players,
                    history,
                    pairs = listOf(
                        PlayerPair(pairList(players[0], players[2]), spinsSinceLastPaired = null),
                        PlayerPair(pairList(players[0], players[3]), spinsSinceLastPaired = null),
                        PlayerPair(pairList(players[0], players[1]), spinsSinceLastPaired = 0),
                        PlayerPair(pairList(players[1], players[2]), spinsSinceLastPaired = null),
                        PlayerPair(pairList(players[1], players[3]), spinsSinceLastPaired = null),
                        PlayerPair(pairList(players[2], players[3]), spinsSinceLastPaired = 0),
                    ),
                    report,
                    emptyList(),
                ),
            )
        }
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

    private fun pairList(player1: Player, player2: Player): List<Record<PartyElement<Player>>> =
        listOf(player1, player2)
            .map { Record(PartyElement(stubPartyId(), it), "test", false, Instant.DISTANT_PAST) }

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
                date = Clock.System.now(),
                pairs = notEmptyListOf(
                    pairOf(players[0], players[1]),
                    pairOf(players[2], players[3]),
                ).withNoPins(),
            ),
        )
        val party = PartyDetails(PartyId("2"), name = "Mathematica")
        val report = perform(ComposeStatisticsAction(party, players, history))
        val heatmapData = perform(CalculateHeatMapAction(players, history, report.spinsUntilFullRotation))
    }) exercise {
        render(jso { wrapper = MemoryRouter }) {
            PartyStatistics(StatisticsQuery.Results(party, players, history, emptyList(), report, heatmapData))
        }
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
        val party = PartyDetails(PartyId("2"), name = "Mathematica")
        val report = perform(ComposeStatisticsAction(party, players, emptyList()))
    }) exercise {
        render(jso { wrapper = MemoryRouter }) {
            PartyStatistics(StatisticsQuery.Results(party, players, emptyList(), emptyList(), report, emptyList()))
        }
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
        val party = PartyDetails(PartyId("2"), name = "Mathematica")
        val history = listOf(
            PairAssignmentDocument(
                id = PairAssignmentDocumentId("${uuid4()}"),
                date = dateTime(2017, 3, 14),
                pairs = notEmptyListOf(
                    pairOf(players[0], players[1]),
                    pairOf(players[2], players[3]),
                ).withNoPins(),
            ),
            PairAssignmentDocument(
                id = PairAssignmentDocumentId("${uuid4()}"),
                date = dateTime(2017, 3, 12),
                pairs = notEmptyListOf(
                    pairOf(players[0], players[1]),
                    pairOf(players[2], players[3]),
                ).withNoPins(),
            ),
        )
        val report = perform(ComposeStatisticsAction(party, players, history))
    }) exercise {
        render(jso { wrapper = MemoryRouter }) {
            PartyStatistics(StatisticsQuery.Results(party, players, history, emptyList(), report, emptyList()))
        }
    } verify {
        within(screen.getByText("Median Spin Duration:").parentElement)
            .getByText("2 days")
    }
}

private fun dateTime(year: Int, month: Int, day: Int): Instant =
    LocalDateTime(year, month, day, 0, 0, 0).toInstant(TimeZone.currentSystemDefault())
