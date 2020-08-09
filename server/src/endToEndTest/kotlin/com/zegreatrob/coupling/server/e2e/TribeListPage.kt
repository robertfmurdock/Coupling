package com.zegreatrob.coupling.server.e2e

import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.server.e2e.external.webdriverio.WebdriverBrowser
import com.zegreatrob.coupling.server.e2e.external.webdriverio.WebdriverElement

object TribeListPage : StyleSyntax {
    override val styles = loadStyles("tribe/TribeList")

    val newTribeButton by getting()

    val tribeCardElements get() = TribeCard.element().all()

    fun tribeCardElement(tribeId: TribeId) = WebdriverElement(
        ".${TribeCard.styles.className}[data-tribe-id=\"${tribeId.value}\"]"
    )

    suspend fun goTo() {
        setLocation("/tribes/")
        waitForPage()
    }

    suspend fun waitForPage() {
        WebdriverBrowser.waitUntil({ element().isPresent() }, 2000, "TribeListPage.waitForPage")
    }
}
