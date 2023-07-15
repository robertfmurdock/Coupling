package com.zegreatrob.coupling.e2e.test

import com.zegreatrob.coupling.action.party.SavePartyCommand
import com.zegreatrob.coupling.action.party.fire
import com.zegreatrob.coupling.e2e.test.CouplingLogin.sdk
import com.zegreatrob.coupling.e2e.test.PartyListPage.getNewPartyButton
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.minassert.assertContains
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.wrapper.wdio.WebdriverBrowser
import kotlin.test.Test

class PartyListPageE2ETest {

    companion object {
        private val twoPartySetup = e2eSetup.extend(beforeAll = {
            val parties = listOf(
                "${randomInt()}-PartyListPageE2ETest-1".let { PartyDetails(it.let(::PartyId), name = it) },
                "${randomInt()}-PartyListPageE2ETest-2".let { PartyDetails(it.let(::PartyId), name = it) },
            )
            sdk.await().apply { parties.forEach { fire(SavePartyCommand(it)) } }
            object {
                val parties = parties
            }
        }).extend(sharedSetup = { PartyListPage.goTo() })
    }

    @Test
    fun shouldHaveSectionForEachParty() = twoPartySetup() exercise {
        PartyListPage.partyCardElements.map { it.text() }
    } verify { listedPartyNames ->
        parties.map { it.name }
            .forEach { expected ->
                listedPartyNames.assertContains(expected)
            }
    }

    @Test
    fun canNavigateToSpecificPartyPage() = twoPartySetup() exercise {
        PartyListPage.partyCardElement(parties[0].id).click()
        PairAssignmentsPage.waitForPage()
    } verify {
        WebdriverBrowser.currentUrl().pathname
            .assertIsEqualTo(resolve(clientBasename, "${parties[0].id.value}/pairAssignments/current/"))
    }

    @Test
    fun canNavigateToTheNewPartyPage() = twoPartySetup() exercise {
        getNewPartyButton().click()
        PartyConfigPage.waitForPage()
    } verify {
        WebdriverBrowser.currentUrl().pathname
            .assertIsEqualTo(resolve(clientBasename, "new-party/"))
    }
}
