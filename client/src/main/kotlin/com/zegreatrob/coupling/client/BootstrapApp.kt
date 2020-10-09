package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.client.SessionConfig.animationsDisabled
import com.zegreatrob.coupling.client.routing.CouplingRouter
import com.zegreatrob.coupling.client.routing.CouplingRouterProps
import com.zegreatrob.coupling.logging.initializeLogging
import com.zegreatrob.coupling.sdk.Sdk
import com.zegreatrob.coupling.sdk.SdkSingleton
import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.Document
import org.w3c.dom.get
import react.createElement

object App : Sdk by SdkSingleton {

    fun bootstrapApp() {
        initializeLogging(developmentMode = false)
        val isSignedIn = window["isAuthenticated"] == true
        react.dom.render(couplingRouterElement(isSignedIn, animationsDisabled), document.viewContainerNode)
    }

    private val Document.viewContainerNode get() = getElementsByClassName("view-container")[0]

    private fun couplingRouterElement(isSignedIn: Boolean, animationsDisabled: Boolean) =
        createElement(CouplingRouter, CouplingRouterProps(isSignedIn, animationsDisabled))
}
