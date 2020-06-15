package com.zegreatrob.coupling.server.e2e

import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.server.e2e.CouplingLogin.sdkProvider
import com.zegreatrob.coupling.server.e2e.external.protractor.browser
import com.zegreatrob.coupling.server.e2e.external.protractor.performClick
import com.zegreatrob.minassert.assertContains
import com.zegreatrob.minassert.assertIsEqualTo
import kotlinx.coroutines.await
import kotlin.test.Test

class TribeListPageE2ETest {

    @Test
    fun shouldHaveSectionForEachTribe() = testTribeListPage(object : TribesContext() {
        val page = TribeListPage
    }) exercise {
        page.tribeCardElements.map { it.getText() }.await().toList()
    } verify { listedTribeNames ->
        tribes.map { it.name }
            .forEach { expected ->
                listedTribeNames.assertContains(expected)
            }
    }

    @Test
    fun canNavigateToSpecificTribePage() = testTribeListPage(object : TribesContext() {
        val page = TribeListPage
    }) exercise {
        with(page) {
            tribeCardElement(tribes[0].id)
                .element(tribeCardHeaderLocator)
                .performClick()
        }
    } verify {
        browser.getCurrentUrl().await()
            .assertIsEqualTo("${browser.baseUrl}/${tribes[0].id.value}/edit/")
    }

    @Test
    fun canNavigateToTheNewTribePage() = testTribeListPage(object : TribesContext() {
        val page = TribeListPage
    }) exercise {
        page.newTribeButton.performClick()
    } verify {
        browser.getCurrentUrl().await()
            .assertIsEqualTo("${browser.baseUrl}/new-tribe/")
    }

    companion object {

        fun <C : TribesContext> testTribeListPage(context: C, additionalActions: suspend C.() -> Unit = {}) =
            e2eSetup(
                contextProvider = { context.tribes = tribeListProvider.await(); context },
                additionalActions = { TribeListPage.goTo();additionalActions() }
            )

        open class TribesContext {
            lateinit var tribes: List<Tribe>
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
