package com.zegreatrob.coupling.client.external.w3c

import org.w3c.dom.Window

interface WindowFunctions {
    val window: Window get() = kotlin.browser.window

    companion object : WindowFunctions
}