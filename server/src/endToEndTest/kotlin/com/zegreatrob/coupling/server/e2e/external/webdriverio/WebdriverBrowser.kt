package com.zegreatrob.coupling.server.e2e.external.webdriverio

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.asPromise
import kotlinx.coroutines.async
import kotlinx.coroutines.await
import org.w3c.dom.url.URL
import kotlin.js.json

object WebdriverBrowser :
    BrowserLoggingSyntax {

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

    suspend fun waitForAlert(): Unit = log(this::waitForAlert) { waitUntil({ isAlertOpen() }) }
    suspend fun isAlertOpen(): Boolean = log(this::isAlertOpen) { browser.isAlertOpen().await() }
    suspend fun acceptAlert(): Unit = log(this::acceptAlert) { browser.acceptAlert().await() }
    suspend fun dismissAlert(): Unit = log(this::dismissAlert) { browser.dismissAlert().await() }
    suspend fun alertText(): String = log(this::alertText) { browser.getAlertText().await() }
    suspend fun element(selector: String): Element = log(this::element) { browser.`$`(selector).await() }
    suspend fun all(selector: String): Array<Element> = log(this::all) { browser.`$$`(selector).await() }
    suspend fun getUrl(): URL = log(this::getUrl) {
        URL(
            browser.getUrl().await()
        )
    }


}