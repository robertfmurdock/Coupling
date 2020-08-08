package com.zegreatrob.coupling.server.e2e.external.webdriverio

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.asPromise
import kotlinx.coroutines.async
import kotlinx.coroutines.await
import org.w3c.dom.url.URL
import kotlin.js.json

object WebdriverBrowser : BrowserLoggingSyntax {

    suspend fun element(selector: String) = log(this::element) { browser.`$`(selector).await() }

    suspend fun all(selector: String): Array<Element> = log(this::all) { browser.`$$`(selector).await() }

    suspend fun waitUntil(
        condition: suspend () -> Boolean,
        timeout: Int = waitToBePresentDuration,
        timeoutMessage: String = ""
    ): Unit = log(this::waitUntil.name) {
        browser.waitUntil(
            {
                GlobalScope.async { condition() }.asPromise()
            }, json(
                "timeout" to timeout,
                "timeoutMsg" to timeoutMessage,
                "interval" to 50
            )
        ).await()
    }

    suspend fun waitForAlert() = log(this::waitForAlert) { waitUntil({ isAlertOpen() }) }
    suspend fun isAlertOpen() = log(this::isAlertOpen) { browser.isAlertOpen().await() }
    suspend fun acceptAlert() = log(this::acceptAlert) { browser.acceptAlert().await() }
    suspend fun dismissAlert() = log(this::dismissAlert) { browser.dismissAlert().await() }
    suspend fun alertText() = log(this::alertText) { browser.getAlertText().await() }
    suspend fun getUrl() = log(this::getUrl) { URL(browser.getUrl().await()) }

}

class WebdriverElement(private val selector: String) {
    private suspend fun element() = WebdriverBrowser.element(selector)

    suspend fun performClick() = element().performClick()
    suspend fun text() = element().text()
    suspend fun all(selector: String) = element().all(selector)

}