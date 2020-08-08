package com.zegreatrob.coupling.server.e2e.external.webdriverio

import com.zegreatrob.coupling.server.e2e.SimpleStyle
import com.zegreatrob.coupling.server.e2e.get
import com.zegreatrob.minassert.assertIsEqualTo
import kotlinx.coroutines.await
import org.w3c.dom.url.URL

interface BrowserSyntax {

    suspend fun setLocation(location: String) {
        val currentUrl = WebdriverBrowser.getUrl()
        if (currentUrl.pathname == location) {
            browser.refresh().await()
        } else if (currentUrl.isNotFromBaseHost()) {
            browser.url(location).await()
        } else {
            alternateImplementation(location)
        }
    }

    private fun URL.isNotFromBaseHost(): Boolean {
        val baseUrlHostname = URL(browser.config["baseUrl"].unsafeCast<String>()).hostname
        return hostname != baseUrlHostname
    }

    @Suppress("UNUSED_ANONYMOUS_PARAMETER")
    private suspend fun alternateImplementation(location: String) {
        browser.executeAsync(
            { loc, done ->
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
            },
            location
        ).await()
    }


    suspend fun browserGoTo(url: String) = browser.url(url).await()

    fun SimpleStyle.locator() = By.className(className)
    suspend fun SimpleStyle.element() =
        WebdriverBrowser.element(locator())
    suspend fun SimpleStyle.elementWithClass(className: String) =
        WebdriverBrowser.element(By.className(this[className]))

    suspend fun waitToArriveAt(expectedPath: String) {
        WebdriverBrowser.waitUntil({
                    try {
                        WebdriverBrowser.getUrl().pathname.startsWith(expectedPath)
                    } catch (bad: Throwable) {
                        false
                    }
                }, 5000, "")

        WebdriverBrowser.getUrl().pathname.startsWith(expectedPath).assertIsEqualTo(true)
    }

    suspend fun waitToArriveAtUrl(expectedUrl: String) {
        WebdriverBrowser.waitUntil({
                    try {
                        WebdriverBrowser.getUrl().toString().startsWith(expectedUrl)
                    } catch (bad: Throwable) {
                        false
                    }
                }, 5000, "")

        WebdriverBrowser.getUrl().toString().startsWith(expectedUrl).assertIsEqualTo(true)
    }

    suspend fun SimpleStyle.element(propertyName: String) = elementWithClass(propertyName)

}