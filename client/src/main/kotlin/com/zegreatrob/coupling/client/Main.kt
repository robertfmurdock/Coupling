package com.zegreatrob.coupling.client

import kotlin.js.Json
import kotlin.js.json

fun main() {
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
