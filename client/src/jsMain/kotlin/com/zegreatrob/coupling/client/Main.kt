package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.client.components.loadImages
import com.zegreatrob.coupling.client.components.loadMarkdown
import js.import.importAsync

fun main() {
    importAsync<Any>("/com/zegreatrob/coupling/client/animations.css")
    importAsync<Any>("/com/zegreatrob/coupling/client/style.css")
    importAsync<Any>("reactjs-popup/dist/index.css")
    importAsync<Any>("mobile-drag-drop/default.css")
    if (isTestRun()) {
        return
    }
    loadImages()
    loadMarkdown()

    applyMobileDragDropPolyfill()

    App.bootstrapApp()
}
