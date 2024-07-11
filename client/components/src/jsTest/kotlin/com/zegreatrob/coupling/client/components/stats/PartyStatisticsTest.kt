package com.zegreatrob.coupling.client.components.stats

import com.zegreatrob.coupling.client.components.TestRouter
import com.zegreatrob.coupling.model.PlayerPair
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.model.party.PartyElement
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.player.defaultPlayer
import com.zegreatrob.coupling.stubmodel.record
import com.zegreatrob.coupling.stubmodel.stubPartyId
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.setup
import com.zegreatrob.wrapper.testinglibrary.react.TestingLibraryReact.render
import com.zegreatrob.wrapper.testinglibrary.react.TestingLibraryReact.screen
import com.zegreatrob.wrapper.testinglibrary.react.TestingLibraryReact.within
import js.objects.jso
import kotlinx.datetime.Instant
import org.w3c.dom.HTMLElement
import org.w3c.dom.asList
import kotlin.test.Test
import kotlin.time.Duration.Companion.days

class PartyStatisticsTest {

    @Test
    fun willShowPairings() = setup(object {
        val players = listOf(
            defaultPlayer.copy("harry", name = "Harry"),
            defaultPlayer.copy("larry", name = "Larry"),
            defaultPlayer.copy("curry", name = "Curly"),
            defaultPlayer.copy("moe", name = "Moe"),
        )
        val party = PartyDetails(PartyId("1"), name = "Mathematica")
    }) exercise {
        render(jso { wrapper = TestRouter }) {
            PartyStatistics(
                party = party,
                players = players,
                pairs = listOf(
                    PlayerPair(pairList(players[0], players[2]), spinsSinceLastPaired = null),
                    PlayerPair(pairList(players[0], players[3]), spinsSinceLastPaired = null),
                    PlayerPair(pairList(players[0], players[1]), spinsSinceLastPaired = 0),
                    PlayerPair(pairList(players[1], players[2]), spinsSinceLastPaired = null),
                    PlayerPair(pairList(players[1], players[3]), spinsSinceLastPaired = null),
                    PlayerPair(pairList(players[2], players[3]), spinsSinceLastPaired = 0),
                ),
                spinsUntilFullRotation = 0,
                medianSpinDuration = null,
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
            defaultPlayer.copy("harry", name = "Harry"),
            defaultPlayer.copy("larry", name = "Larry"),
            defaultPlayer.copy("curry", name = "Curly"),
            defaultPlayer.copy("moe", name = "Moe"),
        )
        val party = PartyDetails(PartyId("2"), name = "Mathematica")
        val heatmapData = listOf(
            listOf(null, 1.0, 0.0, 0.0),
            listOf(1.0, null, 0.0, 0.0),
            listOf(0.0, 0.0, null, 1.0),
            listOf(0.0, 0.0, 1.0, null),
        )
        val pairs = listOf(
            PlayerPair(listOf(players[0], players[1]).toRecords(party.id), recentTimesPaired = 1),
            PlayerPair(listOf(players[0], players[2]).toRecords(party.id), recentTimesPaired = 0),
            PlayerPair(listOf(players[0], players[3]).toRecords(party.id), recentTimesPaired = 0),
            PlayerPair(listOf(players[1], players[2]).toRecords(party.id), recentTimesPaired = 0),
            PlayerPair(listOf(players[1], players[3]).toRecords(party.id), recentTimesPaired = 0),
            PlayerPair(listOf(players[2], players[3]).toRecords(party.id), recentTimesPaired = 1),
        )
    }) exercise {
        render(jso { wrapper = TestRouter }) {
            PartyStatistics(party, players, pairs, 0, null)
        }
    } verify { wrapper ->
        wrapper.baseElement.querySelector("[data-heatmap]")
            .let { it as org.w3c.dom.HTMLDivElement }
            .getAttribute("data-heatmap")
            .assertIsEqualTo(
                heatmapData.joinToString(",") {
                    "[${it.joinToString(",")}]"
                },
            )
    }

    @Test
    fun willShowBasicStatisticsOnSubComponent() = setup(object {
        val players = listOf(
            defaultPlayer.copy("harry", name = "Harry"),
            defaultPlayer.copy("larry", name = "Larry"),
            defaultPlayer.copy("curry", name = "Curly"),
            defaultPlayer.copy("moe", name = "Moe"),
        )
        val party = PartyDetails(PartyId("2"), name = "Mathematica")
    }) exercise {
        render(jso { wrapper = TestRouter }) {
            PartyStatistics(party, players, emptyList(), 3, null)
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
            defaultPlayer.copy("harry", name = "Harry"),
            defaultPlayer.copy("larry", name = "Larry"),
            defaultPlayer.copy("curry", name = "Curly"),
            defaultPlayer.copy("moe", name = "Moe"),
        )
        val party = PartyDetails(PartyId("2"), name = "Mathematica")
    }) exercise {
        render(jso { wrapper = TestRouter }) {
            PartyStatistics(party, players, emptyList(), 0, 2.days)
        }
    } verify {
        within(screen.getByText("Median Spin Duration:").parentElement)
            .getByText("2 days")
    }
}

private fun <E> List<E>.toRecords(id: PartyId): List<Record<PartyElement<E>>> = map { record(id, it) }
