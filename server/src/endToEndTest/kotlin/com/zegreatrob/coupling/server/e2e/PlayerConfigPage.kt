package com.zegreatrob.coupling.server.e2e

import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.server.e2e.ConfigForm.saveButton
import com.zegreatrob.coupling.server.e2e.external.webdriverio.By
import com.zegreatrob.coupling.server.e2e.external.webdriverio.WebdriverBrowser
import com.zegreatrob.coupling.server.e2e.external.webdriverio.waitToBePresentDuration

object PlayerConfigPage : StyleSyntax {
    override val styles = loadStyles("player/PlayerConfig")

    suspend fun playerNameTextField() = WebdriverBrowser.element(By.id("player-name"))
    suspend fun defaultBadgeOption() = WebdriverBrowser.element(By.id("default-badge-option"))
    suspend fun altBadgeOption() = WebdriverBrowser.element(By.id("alt-badge-option"))
    suspend fun adjectiveTextInput() = WebdriverBrowser.element(By.id("adjective-input"))
    suspend fun nounTextInput() = WebdriverBrowser.element(By.id("noun-input"))

    suspend fun goTo(tribeId: TribeId, playerId: String?) {
        setLocation("/${tribeId.value}/player/${playerId}")
        waitForPage()
    }

    suspend fun goToNew(tribeId: TribeId) {
        setLocation("/${tribeId.value}/player/new")
        waitForPage()
    }

    suspend fun waitForPage() {
        element().waitToBePresent()
    }

    suspend fun waitForSaveToComplete(expectedName: String?) {
        WebdriverBrowser.waitUntil(
            { saveButton.enabled() },
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
    suspend fun getPlayerElements() = WebdriverBrowser.all(playerLocator)
    val iconLocator = By.className(styles["playerIcon"])
}

object PlayerRoster : StyleSyntax {
    override val styles = loadStyles("player/PlayerRoster")
    suspend fun getPlayerElements() = WebdriverBrowser.all(".${styles.className} .${PlayerCard.styles["player"]}")
    suspend fun getAddPlayerButton() = getting("addPlayerButton")
}
