package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.logging.initializeLogging
import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.Document
import org.w3c.dom.get
import react.create
import react.dom.client.createRoot

object App {

    fun bootstrapApp() {
        initializeLogging(developmentMode = false)
        createRoot(document.viewContainerNode)
            .render(RootComponent.create { this.clientConfig = windowClientConfig() })
    }

    private val Document.viewContainerNode get() = getElementsByClassName("view-container")[0]!!
}

private fun windowClientConfig() = ClientConfig(
    prereleaseMode = "${window["prereleaseMode"]}".toBooleanStrictOrNull() ?: false,
    auth0ClientId = "${window["auth0ClientId"]}",
    auth0Domain = "${window["auth0Domain"]}",
    basename = "${window["basename"]}",
    expressEnv = "${window["expressEnv"]}",
    webpackPublicPath = "${window["webpackPublicPath"]}",
    websocketHost = "${window["websocketHost"]}",
)
