package com.zegreatrob.coupling.e2e.test

import com.zegreatrob.coupling.e2e.external.setupBrowser
import com.zegreatrob.coupling.e2e.test.webdriverio.BrowserSyntax
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.wrapper.wdio.WebdriverBrowser
import com.zegreatrob.wrapper.wdio.WebdriverElement
import com.zegreatrob.wrapper.wdio.WebdriverElementArray
import com.zegreatrob.wrapper.wdio.browser
import com.zegreatrob.wrapper.wdio.testing.library.RoleOptions
import com.zegreatrob.wrapper.wdio.testing.library.TestingLibraryBrowser
import kotlinx.coroutines.await

private val testingBrowser = setupBrowser(browser)

object HistoryPage : BrowserSyntax {
    private val historyView
        get() = WebdriverElementArray(finder = {
            testingBrowser.findAllByText("History!").await().map { WebdriverElement(finder = { it }) }
        })
    val pairAssignments get() = WebdriverElementArray("[data-pair-assignments-id]")

    suspend fun getDeleteButtons() = TestingLibraryBrowser.getAllByRole("button", RoleOptions(name = "DELETE"))

    suspend fun goTo(partyId: PartyId) {
        WebdriverBrowser.setLocation("/${partyId.value}/history")
        waitForPage()
    }

    suspend fun waitForPage() {
        WebdriverBrowser.waitUntil({ historyView.count() == 2 }, timeoutMessage = "never arrived at history")
    }
}
