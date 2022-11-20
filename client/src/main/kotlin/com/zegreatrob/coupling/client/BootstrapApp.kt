package com.zegreatrob.coupling.client

import browser.document
import browser.window
import com.zegreatrob.coupling.logging.initializeLogging
import dom.Document
import react.create
import react.dom.client.createRoot

object App {

    fun bootstrapApp() {
        initializeLogging(developmentMode = false)
        createRoot(document.viewContainerNode)
            .render(RootComponent.create { this.clientConfig = windowClientConfig() })
    }

    private val Document.viewContainerNode get() = getElementsByClassName("view-container").item(0)!!
}

private fun windowClientConfig() = ClientConfig(
    prereleaseMode = "${window.asDynamic()["prereleaseMode"]}".toBooleanStrictOrNull() ?: false,
    auth0ClientId = "${window.asDynamic()["auth0ClientId"]}",
    auth0Domain = "${window.asDynamic()["auth0Domain"]}",
    basename = "${window.asDynamic()["basename"]}",
    expressEnv = "${window.asDynamic()["expressEnv"]}",
    webpackPublicPath = "${window.asDynamic()["webpackPublicPath"]}",
    websocketHost = "${window.asDynamic()["websocketHost"]}",
)
