package com.zegreatrob.coupling.e2e.test

import com.zegreatrob.coupling.action.party.SaveContributionCommand
import com.zegreatrob.coupling.action.party.SavePartyCommand
import com.zegreatrob.coupling.action.party.fire
import com.zegreatrob.coupling.action.player.DeletePlayerCommand
import com.zegreatrob.coupling.action.player.SavePlayerCommand
import com.zegreatrob.coupling.action.player.fire
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.stubmodel.stubContributionInput
import com.zegreatrob.coupling.stubmodel.stubPartyDetails
import com.zegreatrob.coupling.stubmodel.stubPlayer
import com.zegreatrob.minassert.assertContains
import com.zegreatrob.wrapper.wdio.WebdriverBrowser
import kotlinx.coroutines.await
import kotlin.test.Test

@Suppress("unused")
class ContributionVisualizationPageE2ETest {

    class Context(val pairAssignments: List<PairAssignmentDocument>) {
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
}
