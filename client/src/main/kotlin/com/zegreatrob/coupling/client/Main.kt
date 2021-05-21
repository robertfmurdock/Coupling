package com.zegreatrob.coupling.client

import kotlinx.browser.window
import org.w3c.dom.get
import kotlin.js.Json
import kotlin.js.json

fun main() {
    if (isTestRun())
        return

    console.log("webpackPublicPath", window["webpackPublicPath"])

    js("__webpack_public_path__ = window.webpackPublicPath;")

    js("require('prefixfree')")
    js("require('com/zegreatrob/coupling/client/animations.css')")
    js("require('@fortawesome/fontawesome-free/css/all.css')")

    configureDragDropWebkitMobile()

    App.bootstrapApp()
}

private fun configureDragDropWebkitMobile() {
    val ddwm = kotlinext.js.require("drag-drop-webkit-mobile").unsafeCast<(Json) -> Unit>()
    ddwm(json("enableEnterLeave" to true))
}
