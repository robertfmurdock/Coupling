package com.zegreatrob.coupling.e2e.test

import com.zegreatrob.coupling.action.party.SaveContributionCommand
import com.zegreatrob.coupling.action.party.SavePartyCommand
import com.zegreatrob.coupling.action.party.fire
import com.zegreatrob.coupling.action.player.DeletePlayerCommand
import com.zegreatrob.coupling.action.player.SavePlayerCommand
import com.zegreatrob.coupling.action.player.fire
import com.zegreatrob.coupling.model.pairassignmentdocument.PairingSet
import com.zegreatrob.coupling.stubmodel.stubContributionInput
import com.zegreatrob.coupling.stubmodel.stubPartyDetails
import com.zegreatrob.coupling.stubmodel.stubPlayer
import com.zegreatrob.minassert.assertContains
import com.zegreatrob.minassert.assertIsEqualTo
import kotlin.test.Test
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

@Suppress("unused")
class ContributionOverviewPageE2ETest {

    class Context(val pairAssignments: List<PairingSet>) {
        val page = HistoryPage
    }

    @Test
    fun whenThereAreNoContributionsWillShowSetup() = e2eSetup(object {
        val party = stubPartyDetails()
    }) {
        sdk().fire(SavePartyCommand(party))
    } exercise {
        ContributionOverviewPage.goTo(party.id)
    } verify {
        ContributionOverviewPage.setupInstructions().isDisplayed()
            .assertIsEqualTo(true)
        ContributionOverviewPage.summaryValue("Recent contributions").assertIsEqualTo("0")
        ContributionOverviewPage.summaryValue("With cycle time").assertIsEqualTo("0")
        ContributionOverviewPage.summaryValue("Median cycle time").assertIsEqualTo("Unavailable")
        ContributionOverviewPage.summaryValue("Contributors").assertIsEqualTo("0")
    }

    @Test
    fun summarizesOnlyLatestFiveContributionsAndCountsEachContributorOnce() = e2eSetup(object {
        val party = stubPartyDetails().copy(name = "Contribution summary example")
        val alex = "alex@example.invalid"
        val sam = "sam@example.invalid"
        val contributions = (0..5).map { index ->
            stubContributionInput().copy(
                name = "Change $index",
                dateTime = Instant.parse("2026-11-19T12:0$index:00Z"),
                integrationDateTime = Instant.parse("2026-11-19T12:0$index:00Z"),
                participantEmails = when (index) {
                    0 -> setOf("older@example.invalid")
                    1 -> setOf(alex, sam)
                    else -> setOf(alex)
                },
                cycleTime = when (index) {
                    0 -> 1000.minutes
                    1 -> 10.minutes
                    2 -> 20.minutes
                    3 -> 30.minutes
                    else -> null
                },
            )
        }
    }) {
        sdk().fire(SavePartyCommand(party))
        sdk().fire(SaveContributionCommand(party.id, contributions))
    } exercise {
        ContributionOverviewPage.goTo(party.id)
    } verify {
        ContributionOverviewPage.mostRecentHeader(5).isDisplayed()
            .assertIsEqualTo(true)
        ContributionOverviewPage.summaryValue("Recent contributions").assertIsEqualTo("5")
        ContributionOverviewPage.summaryValue("With cycle time").assertIsEqualTo("3")
        ContributionOverviewPage.summaryValue("Median cycle time").assertIsEqualTo("20m")
        ContributionOverviewPage.summaryValue("Contributors").assertIsEqualTo("2")
    }

    @Test
    fun whenThereAreContributionsWillShowRelatedPlayersEvenWhenDeleted() = e2eSetup(object {
        val party = stubPartyDetails()
        val players = listOf(stubPlayer())
        val deletedPlayers = listOf(stubPlayer())
        val allPlayers = players + deletedPlayers
    }) {
        sdk().fire(SavePartyCommand(party))
        allPlayers.forEach { sdk().fire(SavePlayerCommand(party.id, it)) }
        deletedPlayers.forEach { sdk().fire(DeletePlayerCommand(party.id, it.id)) }
        sdk().fire(
            SaveContributionCommand(
                party.id,
                listOf(stubContributionInput().copy(participantEmails = allPlayers.map { it.email }.toSet())),
            ),
        )
    } exercise {
        ContributionOverviewPage.goTo(party.id)
    } verify {
        ContributionOverviewPage.mostRecentHeader(1)
            .parentElement()
            .all(PlayerCard.playerElements.selector)
            .map { it.attribute("data-player-id") }
            .apply {
                allPlayers.forEach {
                    assertContains(it.id.value.toString())
                }
            }
    }
}
