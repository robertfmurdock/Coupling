package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.logging.initializeLogging
import react.create
import react.dom.client.createRoot
import web.cssom.ClassName
import web.dom.Document
import web.dom.document
import web.window.window

object App {

    fun bootstrapApp() {
        val config = windowClientConfig()
        initializeLogging(developmentMode = false)
        ClientLogCapture.install(config.clientVersion)
        createRoot(document.viewContainerNode)
            .render(RootComponent.create { this.clientConfig = config })
    }

    private val Document.viewContainerNode get() = getElementsByClassName(ClassName("view-container")).item(0)!!
}

private fun windowClientConfig(): ClientConfig {
    val clientVersion = "${window.asDynamic()["clientVersion"]}"
    return ClientConfig(
        prereleaseMode = "${window.asDynamic()["prereleaseMode"]}".toBooleanStrictOrNull() == true,
        auth0ClientId = "${window.asDynamic()["auth0ClientId"]}",
        auth0Domain = "${window.asDynamic()["auth0Domain"]}",
        basename = "${window.asDynamic()["basename"]}",
        expressEnv = "${window.asDynamic()["expressEnv"]}",
        webpackPublicPath = "${window.asDynamic()["webpackPublicPath"]}",
        websocketHost = "${window.asDynamic()["websocketHost"]}",
        clientVersion = clientVersion.takeIf { it.isNotBlank() && it != "undefined" && it != "null" } ?: "unknown",
    )
}
