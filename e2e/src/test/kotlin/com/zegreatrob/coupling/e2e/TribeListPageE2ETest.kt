package com.zegreatrob.coupling.e2e

import com.zegreatrob.coupling.e2e.CouplingLogin.sdkProvider
import com.zegreatrob.coupling.e2e.TribeListPage.newTribeButton
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.minassert.assertContains
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.invoke
import com.zegreatrob.wrapper.wdio.WebdriverBrowser
import kotlin.test.Test

class TribeListPageE2ETest {

    companion object {
        private val twoTribesSetup = e2eSetup.extend(beforeAll = {
            val tribes = listOf(
                "${randomInt()}-TribeListPageE2ETest-1".let { Tribe(it.let(::TribeId), name = it) },
                "${randomInt()}-TribeListPageE2ETest-2".let { Tribe(it.let(::TribeId), name = it) }
            )

            val sdk = sdkProvider.await()
            tribes.forEach { sdk.save(it) }
            object {
                val tribes = tribes
            }
        }).extend(sharedSetup = { TribeListPage.goTo() })
    }

    @Test
    fun shouldHaveSectionForEachTribe() = twoTribesSetup() exercise {
        TribeListPage.tribeCardElements.map { it.text() }
    } verify { listedTribeNames ->
        tribes.map { it.name }
            .forEach { expected ->
                listedTribeNames.assertContains(expected)
            }
    }

    @Test
    fun canNavigateToSpecificTribePage() = twoTribesSetup() exercise {
        TribeListPage.tribeCardElement(tribes[0].id)
            .element(TribeCard.header.selector)
            .click()
        TribeConfigPage.waitForPage()
    } verify {
        WebdriverBrowser.currentUrl().pathname
            .assertIsEqualTo("/${tribes[0].id.value}/edit/")
    }

    @Test
    fun canNavigateToTheNewTribePage() = twoTribesSetup() exercise {
        newTribeButton.click()
        TribeConfigPage.waitForPage()
    } verify {
        WebdriverBrowser.currentUrl().pathname
            .assertIsEqualTo("/new-tribe/")
    }

}
