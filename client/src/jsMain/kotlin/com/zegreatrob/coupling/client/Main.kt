package com.zegreatrob.coupling.client

import kotlin.js.Json
import kotlin.js.json

fun main() {
    if (isTestRun()) {
        return
    }
    require<Unit>("com/zegreatrob/coupling/client/animations.css")
    require<Unit>("com/zegreatrob/coupling/client/style.css")

    configureDragDropWebkitMobile()

    App.bootstrapApp()
}

private fun configureDragDropWebkitMobile() {
    val ddwm = require<(Json) -> Unit>("drag-drop-webkit-mobile")
    ddwm(json("enableEnterLeave" to true))
}

external fun <T> require(module: String): T
