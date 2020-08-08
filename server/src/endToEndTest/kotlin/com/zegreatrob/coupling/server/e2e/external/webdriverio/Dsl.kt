package com.zegreatrob.coupling.server.e2e.external.webdriverio

import com.soywiz.klock.measureTimeWithResult
import kotlinx.coroutines.*
import mu.KotlinLogging
import org.w3c.dom.url.URL
import kotlin.js.Promise
import kotlin.js.json
import kotlin.reflect.KCallable

object By {
    fun className(className: String): String = ".$className"
    fun id(id: String): String = "#$id"
}

const val waitToBePresentDuration = 6000

suspend fun Promise<Element>.waitToBePresent() {
    val element = await()
    element.waitForExist(json("timeout" to waitToBePresentDuration)).await()
}

suspend fun Promise<Element>.isPresent() = await().isExisting().await()
suspend fun Promise<Element>.isEnabled() = await().isEnabled().await()
fun Promise<Element>.isDisplayed() = then { it.isDisplayed() }
suspend fun Promise<Element>.attribute(key: String) = await().getAttribute(key).await()
fun Promise<Element>.performSendKeys(key: String) = then { it.keys(key) }
fun Promise<Element>.performClear() = then { it.clearValue() }
fun Promise<Element>.performClearSetValue(keystrokes: String) = then { it.clearValue(); it.keys(keystrokes) }
suspend fun Promise<Element>.isNotPresent() = then { it.isExisting() }.then { !it }.await()
fun Promise<Element>.element(selector: String): Promise<Element> = then { it.`$`(selector) }
    .unsafeCast<Promise<Element>>()

fun Promise<Element>.all(selector: String): Promise<Array<Element>> = then { it.`$$`(selector) }
    .unsafeCast<Promise<Array<Element>>>()

suspend fun Promise<Element>.performClick() = then { it.click() }.await()
suspend fun Promise<Element>.text() = await().text()

suspend fun Element.isPresent() = isExisting().await()
suspend fun Element.enabled() = isEnabled().await()
suspend fun Element.displayed() = isDisplayed().await()
suspend fun Element.attribute(key: String) = getAttribute(key).await()
suspend fun Element.performSendKeys(key: String) = keys(key).await()
suspend fun Element.performClear() = clearValue().await()
suspend fun Element.performSetValue(value: String) {
    setValue(value).await()
    delay(20)
}

suspend fun Element.performClearSetValue(keystrokes: String) {
    performClear()
    performSetValue(keystrokes)
}

suspend fun Element.performClick() = click().await()

suspend fun Element.text() = getText().await()

suspend fun Element.isNotPresent() = !isExisting().await()
fun Element.element(selector: String): Promise<Element> = `$`(selector)
    .unsafeCast<Promise<Element>>()

fun Element.all(selector: String): Promise<Array<Element>> = `$$`(selector)
    .unsafeCast<Promise<Array<Element>>>()

suspend fun Element.waitToBePresent() = waitForExist(json("timeout" to waitToBePresentDuration)).await()

typealias ElementSelector = Array<Element>

fun Promise<Array<Element>>.count() = then { it.size }

fun Promise<Array<Element>>.get(index: Int): Promise<Element> = then { it[index] }

fun <T> Promise<Array<Element>>.map(transform: (Element) -> T) = then { it.map(transform) }
suspend fun <T> Promise<Array<Element>>.mapSuspend(transform: suspend (Element) -> T) = await()
    .map { transform(it) }
    .toTypedArray()

fun Promise<Array<Element>>.first() = then { it[0] }

private val theLogger by lazy { KotlinLogging.logger("webdriverioLogger") }

interface BrowserLoggingSyntax {
    val logger get() = theLogger

    suspend fun <T> log(workType: KCallable<*>, browserWork: suspend () -> T) = log(workType.name, browserWork)

    suspend fun <T> log(workType: String, browserWork: suspend () -> T): T {
        val measureTimeWithResult = measureTimeWithResult { browserWork() }
        logger.info {
            json("workType" to workType, "duration" to "${measureTimeWithResult.time}")
        }
        return measureTimeWithResult.result
    }
}

object WebdriverBrowser : BrowserLoggingSyntax {

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
    suspend fun getUrl(): URL = log(this::getUrl) { URL(browser.getUrl().await()) }


}