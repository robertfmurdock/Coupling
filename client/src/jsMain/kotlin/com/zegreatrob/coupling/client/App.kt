package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.logging.initializeLogging
import react.create
import react.createContext
import react.dom.client.createRoot
import web.cssom.ClassName
import web.dom.Document
import web.dom.document
import web.window.window

object App {

    fun bootstrapApp() {
        initializeLogging(developmentMode = false)
        createRoot(document.viewContainerNode)
            .render(RootComponent.create { this.clientConfig = windowClientConfig() })
    }

    private val Document.viewContainerNode get() = getElementsByClassName(ClassName("view-container")).item(0)!!
}

private fun windowClientConfig() = ClientConfig(
    prereleaseMode = "${window.asDynamic()["prereleaseMode"]}".toBooleanStrictOrNull() == true,
    auth0ClientId = "${window.asDynamic()["auth0ClientId"]}",
    auth0Domain = "${window.asDynamic()["auth0Domain"]}",
    basename = "${window.asDynamic()["basename"]}",
    expressEnv = "${window.asDynamic()["expressEnv"]}",
    webpackPublicPath = "${window.asDynamic()["webpackPublicPath"]}",
    websocketHost = "${window.asDynamic()["websocketHost"]}",
)

val configContext = createContext(windowClientConfig())
