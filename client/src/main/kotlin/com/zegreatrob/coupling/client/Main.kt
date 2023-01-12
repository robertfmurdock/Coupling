package com.zegreatrob.coupling.client

import kotlinext.js.require
import kotlin.js.Json
import kotlin.js.json

fun main() {
    if (isTestRun()) {
        return
    }
    require("com/zegreatrob/coupling/client/animations.css")
    require("com/zegreatrob/coupling/client/style.css")
    require("history")

    configureDragDropWebkitMobile()

    App.bootstrapApp()
}

private fun configureDragDropWebkitMobile() {
    val ddwm = require("drag-drop-webkit-mobile").unsafeCast<(Json) -> Unit>()
    ddwm(json("enableEnterLeave" to true))
}
