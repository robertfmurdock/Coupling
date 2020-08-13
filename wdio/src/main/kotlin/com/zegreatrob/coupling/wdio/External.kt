package com.zegreatrob.coupling.wdio

import kotlin.js.Json
import kotlin.js.Promise

external val browser: Browser

external interface Browser {
    val config: Json
    fun `$`(selector: String): Promise<Element>
    fun `$$`(selector: String): Promise<Array<Element>>
    fun waitUntil(condition: () -> Promise<Boolean>, options: Json): Promise<Unit>
    fun getUrl(): Promise<String>
    fun acceptAlert(): Promise<Unit>
    fun dismissAlert(): Promise<Unit>
    fun getAlertText(): Promise<String>

    fun navigateTo(location: String): Promise<Unit>
    fun url(url: String): Promise<Unit>

    fun getLogs(s: String): Promise<Array<Json>>
    fun isAlertOpen(): Promise<Boolean>
    fun execute(code: (dynamic) -> dynamic, argument: dynamic)
    fun executeAsync(arg: (dynamic, () -> Unit) -> dynamic, argument: dynamic): Promise<Unit>
    fun refresh(): Promise<Unit>
}

external class Element {
    fun waitForExist(json: Json): Promise<Unit>
    fun isExisting(): Promise<Boolean>
    fun isEnabled(): Promise<Boolean>
    fun isDisplayed(): Promise<Boolean>
    fun click(): Promise<Unit>
    fun getText(): Promise<String>
    fun `$`(@Suppress("UNUSED_PARAMETER") selector: String): Promise<Element>
    fun `$$`(@Suppress("UNUSED_PARAMETER") selector: String): Promise<Array<Element>>
    fun getAttribute(key: String): Promise<String>
    fun clearValue(): Promise<Unit>
    fun keys(keystrokes: String): Promise<Unit>
    fun setValue(value: String): Promise<Unit>
}


