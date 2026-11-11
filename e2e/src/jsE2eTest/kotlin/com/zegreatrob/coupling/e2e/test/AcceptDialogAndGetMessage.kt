package com.zegreatrob.coupling.e2e.test

import com.zegreatrob.wrapper.wdio.browser
import kotlinx.coroutines.await
import kotlin.js.Promise

suspend fun acceptDialogAndGetMessage(triggerFunc: suspend () -> Unit): String {
    lateinit var listener: (dynamic) -> Promise<Any?>
    val textPromise = Promise<String> { resolve, reject ->
        listener = { dialog ->
            val message = dialog.message().unsafeCast<String>()
            dialog.accept().unsafeCast<Promise<Unit>>().then(
                onFulfilled = { resolve(message) },
                onRejected = { reject(it) },
            )
        }
    }
    browser.on("dialog", listener)
    try {
        triggerFunc()
        return textPromise.await()
    } finally {
        browser.removeListener("dialog", listener)
    }
}
