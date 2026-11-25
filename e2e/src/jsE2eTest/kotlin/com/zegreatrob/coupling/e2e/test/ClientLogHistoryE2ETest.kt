package com.zegreatrob.coupling.e2e.test

import com.zegreatrob.coupling.action.party.SavePartyCommand
import com.zegreatrob.coupling.action.party.fire
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.wrapper.wdio.WebdriverBrowser
import com.zegreatrob.wrapper.wdio.testing.library.RoleOptions
import com.zegreatrob.wrapper.wdio.testing.library.TestingLibraryBrowser
import kotlinx.coroutines.await
import kotlin.js.Promise
import kotlin.test.Test

class ClientLogHistoryE2ETest {

    @Test
    fun realActionLogsSurvivePageReloadAndRemainExportable() = sdkSetup(object : SdkContext() {
        val party = PartyDetails(
            id = "${randomInt()}-ClientLogHistoryE2ETest-party".let(::PartyId),
            name = "${randomInt()}-ClientLogHistoryE2ETest-party",
        )
    }) {
        sdk.fire(SavePartyCommand(party))
        clearClientLogStorage()
    } exercise {
        PartyListPage.goTo()
        val eventsBeforeReload = storedClientLogRecords()
        val emittedEvent = eventsBeforeReload.firstOrNull { it.contains("\"command_action\"") }

        WebdriverBrowser.setLocation("/parties/")
        PartyListPage.waitForPage()
        val eventsAfterReload = storedClientLogRecords()

        WebdriverBrowser.setLocation("/${party.id.value}/developer-settings/")
        WebdriverBrowser.waitUntil(
            { TestingLibraryBrowser.getByRole("heading", RoleOptions("Developer settings")).isDisplayed() },
            timeout = 30000,
            timeoutMessage = "Developer settings did not become ready",
        )
        TestingLibraryBrowser.getByRole("button", RoleOptions("Download client logs (JSONL)")).click()
        val exportStatus = TestingLibraryBrowser.getByRole("status", RoleOptions()).text()

        Triple(emittedEvent, eventsAfterReload, exportStatus)
    } verify { (emittedEvent, eventsAfterReload, exportStatus) ->
        (emittedEvent != null).assertIsEqualTo(true)
        emittedEvent.orEmpty().contains("\"clientVersion\"").assertIsEqualTo(true)
        eventsAfterReload.contains(emittedEvent.orEmpty()).assertIsEqualTo(true)
        exportStatus.startsWith("Download started.").assertIsEqualTo(true)
    }
}

private suspend fun clearClientLogStorage() {
    js(
        """
        browser.execute(function() {
            Object.keys(window.localStorage)
                .filter(function(key) { return key.indexOf('coupling:client-log:') === 0; })
                .forEach(function(key) { window.localStorage.removeItem(key); });
        }, undefined)
        """,
    ).unsafeCast<Promise<Unit>>().await()
}

private suspend fun storedClientLogRecords(): List<String> = js(
    """
    browser.execute(function() {
        return Object.keys(window.localStorage)
            .filter(function(key) { return key.indexOf('coupling:client-log:record:') === 0; })
            .sort()
            .map(function(key) { return window.localStorage.getItem(key); })
            .filter(function(value) { return value !== null; });
    }, undefined)
    """,
).unsafeCast<Promise<Array<String>>>().await().toList()
