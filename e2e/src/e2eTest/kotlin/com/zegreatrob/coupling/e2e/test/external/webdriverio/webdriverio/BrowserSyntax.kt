package com.zegreatrob.coupling.e2e.test.external.webdriverio.webdriverio

import com.zegreatrob.coupling.e2e.test.external.webdriverio.SimpleStyle
import com.zegreatrob.coupling.e2e.test.external.webdriverio.get
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.wrapper.wdio.By
import com.zegreatrob.wrapper.wdio.WebdriverBrowser
import com.zegreatrob.wrapper.wdio.WebdriverElement
import com.zegreatrob.wrapper.wdio.WebdriverElementArray
import kotlin.reflect.KProperty

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

    fun SimpleStyle.getting() = StyledElementDelegate(this, this@BrowserSyntax)

    fun SimpleStyle.getAll() = StyledElementArrayDelegate(this, this@BrowserSyntax)

    class StyledElementDelegate(private val style: SimpleStyle, syntax: BrowserSyntax) : BrowserSyntax by syntax {
        operator fun getValue(thisRef: Any?, property: KProperty<*>) =
            WebdriverElement(By.className(style[property.name]))
    }

    class StyledElementArrayDelegate(private val style: SimpleStyle, syntax: BrowserSyntax) : BrowserSyntax by syntax {
        operator fun getValue(thisRef: Any?, property: KProperty<*>) =
            WebdriverElementArray(By.className(style[property.name]))
    }
}
