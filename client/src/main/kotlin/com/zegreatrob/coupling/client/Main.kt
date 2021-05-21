package com.zegreatrob.coupling.client

import kotlin.js.Json
import kotlin.js.json

fun main() {
    console.log("__webpack_public_path__ is", js("__webpack_public_path__"))

    if (isTestRun())
        return
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
