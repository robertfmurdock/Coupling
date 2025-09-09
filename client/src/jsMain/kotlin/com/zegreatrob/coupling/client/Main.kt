package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.client.components.loadImages
import com.zegreatrob.coupling.client.components.loadMarkdown
import kotlin.js.Json
import kotlin.js.json

@JsModule("com/zegreatrob/coupling/client/animations.css")
private external val animations: dynamic

@JsModule("com/zegreatrob/coupling/client/style.css")
private external val style: dynamic

@JsModule("reactjs-popup/dist/index.css")
private external val reactJsPopup: dynamic

@JsModule("drag-drop-webkit-mobile")
external val ddwm: (Json) -> Unit

fun main() {
    if (isTestRun()) {
        return
    }
    loadImages()
    loadMarkdown()
    animations
    style
    reactJsPopup

    configureDragDropWebkitMobile()

    App.bootstrapApp()
}

private fun configureDragDropWebkitMobile() {
    ddwm(json("enableEnterLeave" to true))
}
