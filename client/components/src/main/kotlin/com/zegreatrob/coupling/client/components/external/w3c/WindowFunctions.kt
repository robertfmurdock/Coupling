package com.zegreatrob.coupling.client.components.external.w3c

import org.w3c.dom.Window

interface WindowFunctions {
    val window: Window get() = kotlinx.browser.window

    companion object : WindowFunctions
}

fun (() -> Unit).requireConfirmation(
    confirmMessage: String,
    windowFunctions: WindowFunctions = WindowFunctions,
): () -> Unit = fun() {
    if (windowFunctions.window.confirm(confirmMessage)) {
        invoke()
    }
}
