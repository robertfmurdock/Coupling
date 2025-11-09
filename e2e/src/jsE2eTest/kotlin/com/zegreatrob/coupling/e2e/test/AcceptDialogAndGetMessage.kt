package com.zegreatrob.coupling.e2e.test

import com.zegreatrob.wrapper.wdio.browser
import kotlinx.coroutines.await
import kotlin.js.Promise

suspend fun acceptDialogAndGetMessage(triggerFunc: suspend () -> Unit): String {
    val textPromise = Promise<String> { resolve, _ ->
        browser.asDynamic()["on"]("dialog") { dialog: dynamic ->
            resolve(dialog.message())
            dialog.accept()
        }
    }
    triggerFunc()
    return textPromise.await()
}

fun clearDialogListeners() {
    browser.asDynamic()["removeAllListeners"]("dialog")
}
