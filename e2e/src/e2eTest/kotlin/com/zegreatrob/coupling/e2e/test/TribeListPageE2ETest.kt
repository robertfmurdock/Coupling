package com.zegreatrob.coupling.e2e.test

import com.zegreatrob.coupling.e2e.test.CouplingLogin.sdkProvider
import com.zegreatrob.coupling.e2e.test.TribeListPage.newTribeButton
import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.minassert.assertContains
import com.zegreatrob.minassert.assertIsEqualTo

import com.zegreatrob.wrapper.wdio.WebdriverBrowser
import kotlin.test.Test

class TribeListPageE2ETest {

    companion object {
        private val twoTribesSetup = e2eSetup.extend(beforeAll = {
            val tribes = listOf(
                "${randomInt()}-TribeListPageE2ETest-1".let { Party(it.let(::PartyId), name = it) },
                "${randomInt()}-TribeListPageE2ETest-2".let { Party(it.let(::PartyId), name = it) }
            )
            sdkProvider.await().apply {
                tribes.forEach { it.save() }
            }
            object {
                val tribes = tribes
            }
        }).extend(sharedSetup = { TribeListPage.goTo() })
    }

    @Test
    fun shouldHaveSectionForEachParty() = twoTribesSetup() exercise {
        TribeListPage.tribeCardElements.map { it.text() }
    } verify { listedTribeNames ->
        tribes.map { it.name }
            .forEach { expected ->
                listedTribeNames.assertContains(expected)
            }
    }

    @Test
    fun canNavigateToSpecificTribePage() = twoTribesSetup() exercise {
        TribeListPage.tribeCardElement(tribes[0].id).click()
        PairAssignmentsPage.waitForPage()
    } verify {
        WebdriverBrowser.currentUrl().pathname
            .assertIsEqualTo(resolve(clientBasename, "${tribes[0].id.value}/pairAssignments/current/"))
    }

    @Test
    fun canNavigateToTheNewTribePage() = twoTribesSetup() exercise {
        newTribeButton.click()
        TribeConfigPage.waitForPage()
    } verify {
        WebdriverBrowser.currentUrl().pathname
            .assertIsEqualTo(resolve(clientBasename, "new-tribe/"))
    }

}
