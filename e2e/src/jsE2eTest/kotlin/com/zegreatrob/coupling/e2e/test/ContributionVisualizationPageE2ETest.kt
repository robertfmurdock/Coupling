package com.zegreatrob.coupling.e2e.test

import com.zegreatrob.coupling.action.party.SaveContributionCommand
import com.zegreatrob.coupling.action.party.SavePartyCommand
import com.zegreatrob.coupling.action.party.fire
import com.zegreatrob.coupling.action.player.DeletePlayerCommand
import com.zegreatrob.coupling.action.player.SavePlayerCommand
import com.zegreatrob.coupling.action.player.fire
import com.zegreatrob.coupling.e2e.test.PartyConfigPage.findByRole
import com.zegreatrob.coupling.model.ContributionId
import com.zegreatrob.coupling.model.ContributionInput
import com.zegreatrob.coupling.model.pairassignmentdocument.PairingSet
import com.zegreatrob.coupling.stubmodel.stubContributionInput
import com.zegreatrob.coupling.stubmodel.stubPartyDetails
import com.zegreatrob.coupling.stubmodel.stubPlayer
import com.zegreatrob.minassert.assertContains
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.wrapper.wdio.WebdriverBrowser
import com.zegreatrob.wrapper.wdio.testing.library.RoleOptions
import com.zegreatrob.wrapper.wdio.testing.library.TestingLibraryBrowser.findByText
import com.zegreatrob.wrapper.wdio.testing.library.TestingLibraryBrowser.within
import kotlinx.coroutines.await
import kotlin.test.Test

@Suppress("unused")
class ContributionVisualizationPageE2ETest {

    class Context(val pairAssignments: List<PairingSet>) {
        val page = HistoryPage
    }

    @Test
    fun willShowRelatedPlayersEvenWhenDeleted() = e2eSetup(object {
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
        ContributionVisualizationPage.goTo(party.id)
    } verify {
        WebdriverBrowser
            .all(PlayerCard.playerElements.selector)
            .map { it.getAttribute("data-player-id") }
            .map { it.await() }
            .apply {
                allPlayers.forEach {
                    assertContains(it.id.value.toString())
                }
            }
    }

    @Test
    fun noDataCanViewAllVisualizationsWithoutError() = e2eSetup(object {
        val party = stubPartyDetails()
        val players = listOf(stubPlayer())
        suspend fun styleSelector() = findByRole("combobox", RoleOptions(name = "Visualization Style"))
        suspend fun styleOptions() = within(styleSelector()).getAllByRole("option", RoleOptions())
    }) {
        sdk().fire(SavePartyCommand(party))
        players.forEach { sdk().fire(SavePlayerCommand(party.id, it)) }
        ContributionVisualizationPage.goTo(party.id)
    } exercise {
        (0..<styleOptions().count()).forEach { index ->
            styleSelector().selectByIndex(index)
        }
    } verify {
        (styleOptions().count() > 0)
            .assertIsEqualTo(true)
        styleSelector().isDisplayed()
            .assertIsEqualTo(true)
    }

    @Test
    fun oneContributionWithNullsCanViewAllVisualizationsWithoutError() = e2eSetup(object {
        val party = stubPartyDetails()
        val players = listOf(stubPlayer())
        suspend fun styleSelector() = findByRole("combobox", RoleOptions(name = "Visualization Style"))
        suspend fun timeSelector() = findByRole("combobox", RoleOptions(name = "Time Window"))
        suspend fun styleOptions() = within(styleSelector()).getAllByRole("option", RoleOptions())
        suspend fun playerCard() = findByText(players.first().name)
    }) {
        sdk().fire(SavePartyCommand(party))
        players.forEach { sdk().fire(SavePlayerCommand(party.id, it)) }
        sdk().fire(
            SaveContributionCommand(
                party.id,
                listOf(
                    ContributionInput(
                        contributionId = ContributionId.new(),
                        participantEmails = players.map { it.email }.toSet(),
                        hash = null,
                        label = null,
                        commitCount = null,
                        ease = null,
                        semver = null,
                        integrationDateTime = null,
                        firstCommitDateTime = null,
                        link = null,
                        firstCommit = null,
                        dateTime = null,
                        story = null,
                        cycleTime = null,
                        name = null,
                    ),
                ),
            ),
        )
        ContributionVisualizationPage.goTo(party.id)
        timeSelector().selectByVisibleText("All")
        playerCard().click()
    } exercise {
        (0..<styleOptions().count()).forEach { index ->
            styleSelector().selectByIndex(index)
        }
    } verify {
        (styleOptions().count() > 0)
            .assertIsEqualTo(true)
        styleSelector().isDisplayed()
            .assertIsEqualTo(true)
    }
}
