package com.zegreatrob.coupling.sdk

import kotlinx.browser.window
import org.w3c.dom.Window
import org.w3c.dom.get

actual fun basename(): String? {
    return if (js("global.window").unsafeCast<Window?>() != null) "${window["basename"]}" else ""
}
