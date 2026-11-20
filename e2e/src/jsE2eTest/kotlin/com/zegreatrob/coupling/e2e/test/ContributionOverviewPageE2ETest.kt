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
import kotlinx.coroutines.await
import kotlin.js.Promise
import kotlin.test.Test
import kotlin.time.Duration.Companion.minutes

@JsName("browser")
private external object SummaryCaptureBrowser {
    fun saveScreenshot(path: String): Promise<Unit>
    fun setWindowSize(width: Int, height: Int): Promise<Unit>
}

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
    fun summarizesRecentContributionsAndCountsEachContributorOnce() = e2eSetup(object {
        val party = stubPartyDetails().copy(name = "Contribution summary example")
        val alex = "alex@example.invalid"
        val sam = "sam@example.invalid"
        val contributions = listOf(
            stubContributionInput().copy(name = "First change", participantEmails = setOf(alex), cycleTime = 10.minutes),
            stubContributionInput().copy(name = "Second change", participantEmails = setOf(alex, sam), cycleTime = null),
        )
    }) {
        sdk().fire(SavePartyCommand(party))
        sdk().fire(SaveContributionCommand(party.id, contributions))
    } exercise {
        ContributionOverviewPage.goTo(party.id)
    } verify {
        ContributionOverviewPage.mostRecentHeader(2).isDisplayed()
            .assertIsEqualTo(true)
        SummaryCaptureBrowser.setWindowSize(1280, 900).await()
        SummaryCaptureBrowser.saveScreenshot("/private/tmp/cpl23-after-desktop.png").await()
        SummaryCaptureBrowser.setWindowSize(390, 844).await()
        SummaryCaptureBrowser.saveScreenshot("/private/tmp/cpl23-after-mobile.png").await()
        SummaryCaptureBrowser.setWindowSize(1280, 900).await()
        ContributionOverviewPage.summaryValue("Recent contributions").assertIsEqualTo("2")
        ContributionOverviewPage.summaryValue("With cycle time").assertIsEqualTo("1")
        ContributionOverviewPage.summaryValue("Median cycle time").assertIsEqualTo("10m")
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
