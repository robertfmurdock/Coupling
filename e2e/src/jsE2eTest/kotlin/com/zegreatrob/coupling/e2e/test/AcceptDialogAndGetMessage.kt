package com.zegreatrob.coupling.e2e.test

import com.zegreatrob.wrapper.wdio.browser
import kotlinx.coroutines.await
import kotlin.js.Promise

suspend fun acceptDialogAndGetMessage(triggerFunc: suspend () -> Unit): String {
    val textPromise = Promise<String> { resolve, _ ->
        browser.on("dialog") { dialog: dynamic ->
            println("message <${dialog.message()}>")
            resolve(dialog.message())
            dialog.accept()
        }
    }
    triggerFunc()
    val await = textPromise.await()
    browser.removeAllListeners("dialog")
    return await
}
