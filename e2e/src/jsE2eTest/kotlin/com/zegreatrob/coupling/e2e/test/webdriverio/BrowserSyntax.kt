package com.zegreatrob.coupling.e2e.test.webdriverio

import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.wrapper.wdio.WebdriverBrowser

interface BrowserSyntax {
    suspend fun waitToArriveAt(expectedPath: String) {
        WebdriverBrowser.waitUntil({
            try {
                WebdriverBrowser.currentUrl().pathname.startsWith(expectedPath)
            } catch (_: Throwable) {
                false
            }
        }, 5000, "")

        WebdriverBrowser.currentUrl().pathname.startsWith(expectedPath).assertIsEqualTo(true)
    }

    suspend fun waitToArriveAtUrl(expectedUrl: String, timeout: Int? = 5000) {
        WebdriverBrowser.waitUntil({
            try {
                WebdriverBrowser.currentUrl()
                    .toString().startsWith(expectedUrl)
            } catch (_: Throwable) {
                false
            }
        }, timeout, "")

        WebdriverBrowser.currentUrl().toString().startsWith(expectedUrl).assertIsEqualTo(true)
    }
}
