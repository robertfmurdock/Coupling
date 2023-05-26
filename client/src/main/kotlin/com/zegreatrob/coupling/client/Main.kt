package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.client.external.react.ga.ReactGA
import kotlinext.js.require
import kotlin.js.Json
import kotlin.js.json

fun main() {
    if (isTestRun()) {
        return
    }
    ReactGA.initialize("G-F28M9PKERE")
    require("com/zegreatrob/coupling/client/animations.css")
    require("com/zegreatrob/coupling/client/style.css")

    configureDragDropWebkitMobile()

    App.bootstrapApp()
}

private fun configureDragDropWebkitMobile() {
    val ddwm = require("drag-drop-webkit-mobile").unsafeCast<(Json) -> Unit>()
    ddwm(json("enableEnterLeave" to true))
}
