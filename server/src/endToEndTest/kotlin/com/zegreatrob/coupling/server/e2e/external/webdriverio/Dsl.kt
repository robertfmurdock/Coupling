package com.zegreatrob.coupling.server.e2e.external.webdriverio

import kotlinx.coroutines.await
import kotlinx.coroutines.delay
import kotlin.js.Promise
import kotlin.js.json

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
