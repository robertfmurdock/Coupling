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
    }

    @Test
    fun whenThereAreContributionsWillContent() = e2eSetup(object {
        val party = stubPartyDetails()
    }) {
        sdk().fire(SavePartyCommand(party))
        sdk().fire(SaveContributionCommand(party.id, listOf(stubContributionInput())))
    } exercise {
        ContributionOverviewPage.goTo(party.id)
    } verify {
        ContributionOverviewPage.mostRecentHeader(1).isDisplayed()
            .assertIsEqualTo(true)
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
