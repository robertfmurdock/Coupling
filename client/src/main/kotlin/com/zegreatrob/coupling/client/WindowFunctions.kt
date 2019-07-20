package com.zegreatrob.coupling.client

import org.w3c.dom.Window

interface WindowFunctions {
    val window: Window get() = kotlin.browser.window
}