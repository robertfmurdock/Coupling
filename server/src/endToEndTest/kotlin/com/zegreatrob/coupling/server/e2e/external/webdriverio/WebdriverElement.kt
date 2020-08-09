package com.zegreatrob.coupling.server.e2e.external.webdriverio

import kotlinx.coroutines.await
import kotlin.js.Promise
import kotlin.js.json

class WebdriverElement(
    val selector: String = "",
    private val finder: suspend () -> Element = { WebdriverBrowser.element(selector) }
) : BrowserLoggingSyntax {
    internal suspend fun element() = finder()

    fun all() = WebdriverElementArray(selector)

    fun all(selector: String) = if (this.selector == "")
        WebdriverElementArray { element().all(selector).await().map { WebdriverElement { it } } }
    else
        WebdriverElementArray("${this.selector} $selector")

    fun element(selector: String): WebdriverElement = if (this.selector == "")
        WebdriverElement { element().element(selector).await() }
    else
        WebdriverElement("${this.selector} $selector")

    private fun Element.all(selector: String): Promise<Array<Element>> = `$$`(selector)
        .unsafeCast<Promise<Array<Element>>>()

    private fun Element.element(selector: String): Promise<Element> = `$`(selector)
        .unsafeCast<Promise<Element>>()

    suspend fun click() = log(this::click) { element().click().await() }
    suspend fun text() = log(this::text) { element().getText().await() }
    suspend fun attribute(name: String) = log(this::attribute) { element().getAttribute(name).await() }
    suspend fun isPresent() = log(this::isPresent) { element().isExisting().await() }
    suspend fun isEnabled() = log(this::isEnabled) { element().isEnabled().await() }
    suspend fun isDisplayed() = log(this::isDisplayed) { element().isDisplayed().await() }
    suspend fun setValue(value: String) = log(this::setValue) { element().setValue(value).await() }
    suspend fun clearSetValue(value: String) = log(this::clearSetValue) {
        element().clearValue().await()
        element().setValue(value).await()
    }

    suspend fun waitToExist() = log(this::waitToExist) {
        element().waitForExist(json("timeout" to waitToBePresentDuration)).await()
    }

}