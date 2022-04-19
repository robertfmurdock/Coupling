package com.zegreatrob.coupling.e2e.test

import com.zegreatrob.coupling.e2e.test.ConfigForm.saveButton
import com.zegreatrob.coupling.e2e.test.webdriverio.waitToBePresentDuration
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.wrapper.wdio.By
import com.zegreatrob.wrapper.wdio.WebdriverBrowser
import com.zegreatrob.wrapper.wdio.WebdriverElement
import com.zegreatrob.wrapper.wdio.WebdriverElementArray

object PlayerConfigPage : StyleSyntax {
    override val styles = loadStyles("player/PlayerConfig")

    fun playerNameTextField() = WebdriverElement(By.id("player-name"))
    fun defaultBadgeOption() = WebdriverElement(By.id("default-badge-option"))
    fun altBadgeOption() = WebdriverElement(By.id("alt-badge-option"))
    fun adjectiveTextInput() = WebdriverElement(By.id("adjective-input"))
    fun nounTextInput() = WebdriverElement(By.id("noun-input"))

    suspend fun goTo(tribeId: PartyId, playerId: String?) {
        WebdriverBrowser.setLocation("/${tribeId.value}/player/$playerId")
        waitForPage()
    }

    suspend fun goToNew(tribeId: PartyId) {
        WebdriverBrowser.setLocation("/${tribeId.value}/player/new")
        waitForPage()
    }

    suspend fun waitForPage() {
        element().waitToExist()
    }

    suspend fun waitForSaveToComplete(expectedName: String?) {
        WebdriverBrowser.waitUntil(
            { saveButton.isEnabled() },
            waitToBePresentDuration,
            "PlayerConfig.waitForSaveButtonEnable"
        )

        WebdriverBrowser.waitUntil({
            val playerName = PlayerRoster.element().all(PlayerCard.header.selector)
                .first()
                .text()

            (playerName == expectedName)
        }, 100, "PlayerConfig.waitForSave.nameIncluded")
    }
}

object PlayerCard : StyleSyntax {
    override val styles = loadStyles("player/PlayerCard")
    val playerLocator = By.className(styles["player"])
    val header by getting()
    val playerElements = WebdriverElementArray(playerLocator)
    val iconLocator = By.className(styles["playerIcon"])
}

object PlayerRoster : StyleSyntax {
    override val styles = loadStyles("player/PlayerRoster")
    val playerElements = styles.element.all(PlayerCard.playerElements.selector)
    suspend fun getAddPlayerButton() = getting("addPlayerButton")
}
