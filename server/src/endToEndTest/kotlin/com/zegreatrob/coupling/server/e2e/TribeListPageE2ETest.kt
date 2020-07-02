package com.zegreatrob.coupling.server.e2e

import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.server.e2e.CouplingLogin.sdkProvider
import com.zegreatrob.coupling.server.e2e.external.protractor.browser
import com.zegreatrob.coupling.server.e2e.external.protractor.performClick
import com.zegreatrob.minassert.assertContains
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.invoke
import kotlinx.coroutines.await
import kotlin.test.Test

class TribeListPageE2ETest {

    private val twoTribesSetup = e2eSetup.extend(beforeAll = {
        val tribes = listOf(
            "${randomInt()}-TribeListPageE2ETest-1".let { Tribe(it.let(::TribeId), name = it) },
            "${randomInt()}-TribeListPageE2ETest-2".let { Tribe(it.let(::TribeId), name = it) }
        )

        val sdk = sdkProvider.await()
        tribes.forEach { sdk.save(it) }

        TribeListPage.goTo()
        object {
            val tribes = tribes
        }
    })

    @Test
    fun shouldHaveSectionForEachTribe() = twoTribesSetup() exercise {
        TribeListPage.tribeCardElements.map { it.getText() }.await().toList()
    } verify { listedTribeNames ->
        tribes.map { it.name }
            .forEach { expected ->
                listedTribeNames.assertContains(expected)
            }
    }

    @Test
    fun canNavigateToSpecificTribePage() = twoTribesSetup() exercise {
        TribeListPage.tribeCardElement(tribes[0].id)
            .element(TribeListPage.tribeCardHeaderLocator)
            .performClick()
    } verify {
        browser.getCurrentUrl().await()
            .assertIsEqualTo("${browser.baseUrl}/${tribes[0].id.value}/edit/")
    }

    @Test
    fun canNavigateToTheNewTribePage() = twoTribesSetup() exercise {
        TribeListPage.newTribeButton.performClick()
    } verify {
        browser.getCurrentUrl().await()
            .assertIsEqualTo("${browser.baseUrl}/new-tribe/")
    }

}
