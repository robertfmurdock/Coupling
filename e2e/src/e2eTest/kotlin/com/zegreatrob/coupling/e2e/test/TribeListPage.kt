package com.zegreatrob.coupling.e2e.test

import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.wrapper.wdio.WebdriverBrowser
import com.zegreatrob.wrapper.wdio.WebdriverElement

object TribeListPage : StyleSyntax {
    override val styles = loadStyles("tribe/TribeList")

    val newTribeButton by getting()

    val tribeCardElements get() = TribeCard.element().all()

    fun tribeCardElement(tribeId: PartyId) = WebdriverElement(
        ".${TribeCard.styles.className}[data-tribe-id=\"${tribeId.value}\"]"
    )

    suspend fun goTo() {
        WebdriverBrowser.setLocation("/tribes/")
        waitForPage()
    }

    suspend fun waitForPage() {
        WebdriverBrowser.waitUntil({ element().isPresent() }, 2000, "TribeListPage.waitForPage")
    }
}
