package com.zegreatrob.coupling.server.e2e

import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.server.e2e.ConfigForm.saveButton
import com.zegreatrob.coupling.server.e2e.external.webdriverio.*

object PlayerConfigPage : StyleSyntax {
    override val styles = loadStyles("player/PlayerConfig")

    fun playerNameTextField() = WebdriverElement(By.id("player-name"))
    fun defaultBadgeOption() = WebdriverElement(By.id("default-badge-option"))
    fun altBadgeOption() = WebdriverElement(By.id("alt-badge-option"))
    fun adjectiveTextInput() = WebdriverElement(By.id("adjective-input"))
    fun nounTextInput() = WebdriverElement(By.id("noun-input"))

    suspend fun goTo(tribeId: TribeId, playerId: String?) {
        setLocation("/${tribeId.value}/player/${playerId}")
        waitForPage()
    }

    suspend fun goToNew(tribeId: TribeId) {
        setLocation("/${tribeId.value}/player/new")
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
