package com.zegreatrob.coupling.server.e2e

import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.server.e2e.CouplingLogin.sdkProvider
import com.zegreatrob.coupling.server.e2e.external.protractor.browser
import com.zegreatrob.coupling.server.e2e.external.protractor.performClick
import com.zegreatrob.minassert.assertContains
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.setupAsync
import com.zegreatrob.testmints.async.testAsync
import kotlinx.coroutines.await
import kotlin.test.Test

class TribeListPageE2ETest {

    @Test
    fun shouldHaveSectionForEachTribe() = testTribeListPage { tribes ->
        setupAsync(TribeListPage) exerciseAsync {
            tribeCardElements.map { it.getText() }.await().toList()
        } verifyAsync { listedTribeNames ->
            tribes.map { it.name }
                .forEach { expected ->
                    listedTribeNames.assertContains(expected)
                }
        }
    }

    @Test
    fun canNavigateToSpecificTribePage() = testTribeListPage { tribes ->
        setupAsync(TribeListPage) exerciseAsync {
            tribeCardElement(tribes[0].id)
                .element(tribeCardHeaderLocator)
                .performClick()
        } verifyAsync {
            browser.getCurrentUrl().await()
                .assertIsEqualTo("${browser.baseUrl}/${tribes[0].id.value}/edit/")
        }
    }

    @Test
    fun canNavigateToTheNewTribePage() = testTribeListPage {
        setupAsync(TribeListPage) exerciseAsync {
            newTribeButton.performClick()
        } verifyAsync {
            browser.getCurrentUrl().await()
                .assertIsEqualTo("${browser.baseUrl}/new-tribe/")
        }
    }

    companion object {
        fun testTribeListPage(test: suspend (List<Tribe>) -> Unit) = testAsync {
            val tribes = tribeListProvider.await()
            TribeListPage.goTo()
            test(tribes)
        }

        private val tribeListProvider by lazyDeferred {
            val sdk = sdkProvider.await()
            listOf(
                "${randomInt()}-TribeListPageE2ETest-1".let { Tribe(it.let(::TribeId), name = it) },
                "${randomInt()}-TribeListPageE2ETest-2".let { Tribe(it.let(::TribeId), name = it) }
            ).apply { forEach { sdk.save(it) } }
        }
    }
}
