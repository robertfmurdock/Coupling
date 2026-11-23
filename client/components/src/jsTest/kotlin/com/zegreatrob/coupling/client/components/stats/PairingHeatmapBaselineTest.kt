package com.zegreatrob.coupling.client.components.stats

import com.zegreatrob.coupling.model.PlayerPair
import com.zegreatrob.coupling.stubmodel.record
import com.zegreatrob.coupling.stubmodel.stubPartyId
import com.zegreatrob.coupling.stubmodel.stubPlayer
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.asyncSetup
import com.zegreatrob.testmints.setup
import com.zegreatrob.wrapper.testinglibrary.react.RoleOptions
import com.zegreatrob.wrapper.testinglibrary.react.TestingLibraryReact.render
import com.zegreatrob.wrapper.testinglibrary.react.TestingLibraryReact.screen
import com.zegreatrob.wrapper.testinglibrary.react.TestingLibraryReact.within
import com.zegreatrob.wrapper.testinglibrary.userevent.UserEvent
import org.w3c.dom.Element
import org.w3c.dom.HTMLDetailsElement
import kotlin.test.Test

class PairingHeatmapBaselineTest {
    @Test
    fun describesAnEvenSplitWithoutRecommendingPairingDecisions() = asyncSetup(object {
        val players = List(4) { stubPlayer() }
        val actor = UserEvent.setup()
    }) {
        render { PairingHeatmapBaseline(players, emptyList(), 3, 6) }
    } exercise {
        actor.click(screen.getByText("About this comparison"))
    } verify {
        val distribution = screen.getByRole("group", RoleOptions(name = "Pairing distribution"))
        within(distribution).getByText("Even split per pair")
        within(distribution).getByText("2.0")
        screen.queryByText("Expected per pair").assertIsEqualTo(null)
        screen.queryByText("Within ±1 of reference").assertIsEqualTo(null)
    }

    @Test
    fun keepsTheKeyAndReferenceDistinctWithExplanationOnDemand() = asyncSetup(object {
        val players = List(4) { stubPlayer() }
        val actor = UserEvent.setup()
    }) {
        render { PairingHeatmapBaseline(players, emptyList(), 3, 6) }
    } exercise {
        actor.click(screen.getByText("About this comparison"))
    } verify {
        within(screen.getByRole("group", RoleOptions(name = "Heatmap key"))).getByText("0 pairings")
        val scorecard = screen.getByRole("group", RoleOptions(name = "Pairing distribution"))
        within(scorecard).getByText("2.0")
        within(scorecard).getByText("6 saved spins")
        val explanation = screen.getByText("About this comparison").closest("details") as HTMLDetailsElement
        explanation.open.assertIsEqualTo(true)
        val breakdown = screen.getByText("Pair breakdown").closest("details") as HTMLDetailsElement
        breakdown.open.assertIsEqualTo(false)
        within(explanation).getByText("What the counts would be if pairings were spread evenly across the current team.")
    }

    @Test
    fun pairDataShowsCountsDifferenceAndLastPairing() = asyncSetup(object {
        val actor = UserEvent.setup()
        val players = listOf(stubPlayer().copy(name = "Alex"), stubPlayer().copy(name = "Blair"), stubPlayer().copy(name = "Casey"))
        val partyId = stubPartyId()
        val pairs = listOf(
            PlayerPair(players.take(2).map { record(partyId, it) }, recentTimesPaired = 4, spinsSinceLastPaired = 0, pairAssignmentHistory = emptyList()),
            PlayerPair(listOf(players[0], players[2]).map { record(partyId, it) }, recentTimesPaired = 0, spinsSinceLastPaired = null, pairAssignmentHistory = emptyList()),
            PlayerPair(players.drop(1).map { record(partyId, it) }, recentTimesPaired = 2, spinsSinceLastPaired = 1, pairAssignmentHistory = emptyList()),
        )
    }) {
        render { PairingHeatmapBaseline(players, pairs, 3, 6) }
    } exercise {
        actor.click(screen.getByText("Pair breakdown"))
    } verify {
        within(screen.getByRole("group", RoleOptions(name = "Pairing distribution"))).getByText("2.0")
        val table = screen.getByRole("table")
        val rows = table.querySelectorAll("tbody tr")
        (0 until rows.length).map { index ->
            val cells = (rows.item(index) as Element).querySelectorAll("th, td")
            (0 until cells.length).map { cells.item(it)?.textContent }
        }.assertIsEqualTo(
            listOf(
                listOf("Alex / Blair", "4", "+2.0", "0"),
                listOf("Alex / Casey", "0", "-2.0", "Never paired"),
                listOf("Blair / Casey", "2", "0.0", "1"),
            ),
        )
        within(table).getByText("Recent pairings · even split 2.0 per pair")
        (0 until rows.length).map { index ->
            (rows.item(index) as Element).querySelectorAll("td").item(1)?.let { (it as Element).getAttribute("aria-label") }
        }.assertIsEqualTo(listOf("2.0 more than an even split", "2.0 fewer than an even split", "Same count as an even split"))
    }

    @Test
    fun missingCountsAreUnavailableRatherThanZero() = setup(object {
        val players = listOf(stubPlayer().copy(name = "Alex"), stubPlayer().copy(name = "Blair"))
    }) exercise {
        render { PairingHeatmapBaseline(players, emptyList(), 1, null) }
    } verify { result ->
        screen.getByRole("group", RoleOptions(name = "Pairing distribution")).querySelector("dd")?.textContent.assertIsEqualTo("Unavailable")
        result.baseElement.querySelector("tbody tr")?.textContent
            .assertIsEqualTo("Alex / BlairUnavailableUnavailableUnavailable")
    }
}
