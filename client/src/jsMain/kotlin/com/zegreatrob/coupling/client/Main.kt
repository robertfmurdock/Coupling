package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.client.components.loadImages
import com.zegreatrob.coupling.client.components.loadMarkdown
import js.import.importAsync
import kotlin.js.Json
import kotlin.js.json

@JsModule("drag-drop-webkit-mobile")
external val ddwm: (Json) -> Unit

fun main() {
    importAsync<Any>("/com/zegreatrob/coupling/client/animations.css")
    importAsync<Any>("/com/zegreatrob/coupling/client/style.css")
    importAsync<Any>("reactjs-popup/dist/index.css")
    if (isTestRun()) {
        return
    }
    loadImages()
    loadMarkdown()

    configureDragDropWebkitMobile()

    App.bootstrapApp()
}

private fun configureDragDropWebkitMobile() {
    ddwm(json("enableEnterLeave" to true))
}
