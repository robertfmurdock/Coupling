package com.zegreatrob.coupling.e2e.test.webdriverio

import com.zegreatrob.coupling.e2e.test.SimpleStyle
import com.zegreatrob.coupling.e2e.test.get
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.wrapper.wdio.By
import com.zegreatrob.wrapper.wdio.WebdriverBrowser
import com.zegreatrob.wrapper.wdio.WebdriverElement

interface BrowserSyntax {

    val SimpleStyle.locator get() = By.className(className)

    val SimpleStyle.element get() = WebdriverElement(locator)

    fun SimpleStyle.elementWithClass(className: String) = WebdriverElement(By.className(this[className]))

    suspend fun waitToArriveAt(expectedPath: String) {
        WebdriverBrowser.waitUntil({
            try {
                WebdriverBrowser.currentUrl().pathname.startsWith(expectedPath)
            } catch (bad: Throwable) {
                false
            }
        }, 5000, "")

        WebdriverBrowser.currentUrl().pathname.startsWith(expectedPath).assertIsEqualTo(true)
    }

    suspend fun waitToArriveAtUrl(expectedUrl: String) {
        WebdriverBrowser.waitUntil({
            try {
                WebdriverBrowser.currentUrl()
                    .toString().startsWith(expectedUrl)
            } catch (bad: Throwable) {
                false
            }
        }, 5000, "")

        WebdriverBrowser.currentUrl().toString().startsWith(expectedUrl).assertIsEqualTo(true)
    }

    suspend fun SimpleStyle.element(propertyName: String) = elementWithClass(propertyName)
}
