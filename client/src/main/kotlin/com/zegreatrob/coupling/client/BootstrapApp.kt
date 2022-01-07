package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.logging.initializeLogging
import com.zegreatrob.coupling.sdk.Sdk
import com.zegreatrob.coupling.sdk.SdkSingleton
import kotlinx.browser.document
import org.w3c.dom.Document
import org.w3c.dom.get
import react.createElement
import react.dom.render

object App : Sdk by SdkSingleton {

    fun bootstrapApp() {
        initializeLogging(developmentMode = false)
        render(createElement { RootComponent() }, document.viewContainerNode)
    }

    private val Document.viewContainerNode get() = getElementsByClassName("view-container")[0]!!

}
