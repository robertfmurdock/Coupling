package com.zegreatrob.coupling.wdio

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.asPromise
import kotlinx.coroutines.async
import kotlinx.coroutines.await
import org.w3c.dom.url.URL
import kotlin.js.json

object WebdriverBrowser : BrowserLoggingSyntax {

    suspend fun element(selector: String) = log(WebdriverBrowser::element) { browser.`$`(selector).await() }

    suspend fun all(selector: String): Array<Element> = log(
        WebdriverBrowser::all
    ) { browser.`$$`(selector).await() }

    suspend fun waitUntil(
        condition: suspend () -> Boolean,
        timeout: Int? = null,
        timeoutMessage: String = ""
    ): Unit = log(this::waitUntil.name) {
        val options = json("timeoutMsg" to timeoutMessage)
            .let { if (timeout != null) it.add(json("timeout" to timeout)) else it }
        browser.waitUntil({ GlobalScope.async { condition() }.asPromise() }, options)
            .await()
    }

    private val baseUrl get() = URL(browser.config["baseUrl"].unsafeCast<String>())

    suspend fun waitForAlert() = log(WebdriverBrowser::waitForAlert) {
        waitUntil({ isAlertOpen() })
    }

    suspend fun isAlertOpen() = log(WebdriverBrowser::isAlertOpen) { browser.isAlertOpen().await() }
    suspend fun acceptAlert() = log(WebdriverBrowser::acceptAlert) { browser.acceptAlert().await() }
    suspend fun dismissAlert() = log(WebdriverBrowser::dismissAlert) { browser.dismissAlert().await() }
    suspend fun alertText() = log(WebdriverBrowser::alertText) { browser.getAlertText().await() }
    suspend fun currentUrl() = log(WebdriverBrowser::currentUrl) {
        URL(
            browser.getUrl().await()
        )
    }

    suspend fun refresh() = browser.refresh().await()
    suspend fun setUrl(url: String) = browser.url(url).await()
    suspend fun getLogs() = browser.getLogs("browser").await().toList()

    suspend fun executeAsync(argument: dynamic, arg: (dynamic, () -> Unit) -> dynamic) =
        browser.executeAsync(arg, argument).await()

    suspend fun setLocation(location: String) {
        val currentUrl = currentUrl()
        if (currentUrl.pathname == location) {
            refresh()
        } else if (currentUrl.isNotFromBaseHost()) {
            setUrl(location)
        } else {
            alternateImplementation(location)
        }
    }

    private fun URL.isNotFromBaseHost() = hostname != baseUrl.hostname

    @Suppress("UNUSED_ANONYMOUS_PARAMETER")
    private suspend fun alternateImplementation(location: String) {
        executeAsync(location) { loc, done ->
            js(
                """
                        var wait = function() {
                            window.setTimeout(function() {
                                if (loc === window.location.pathname) {
                                    done()
                                } else {
                                    wait()
                                }
                            }, 5)
                        }
                        
                        if(window.pathSetter){
                            window.pathSetter(loc)
                            wait()
                        } else {
                            done()
                            window.location.pathname = loc
                        }
                    """
            )
        }
    }

}

