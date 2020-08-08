package com.zegreatrob.coupling.server.e2e

import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.server.e2e.PlayerCard.playerCardStyles
import com.zegreatrob.coupling.server.e2e.external.webdriverio.*

object PlayerConfig : StyleSyntax {
    override val styles = loadStyles("player/PlayerConfig")

    suspend fun playerNameTextField() = element(By.id("player-name"))
    suspend fun defaultBadgeOption() = element(By.id("default-badge-option"))
    suspend fun altBadgeOption() = element(By.id("alt-badge-option"))
    suspend fun adjectiveTextInput() = element(By.id("adjective-input"))
    suspend fun nounTextInput() = element(By.id("noun-input"))

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
            waitUntil(
            { ConfigForm.getSaveButton().enabled() },
                waitToBePresentDuration,
                "PlayerConfig.waitForSaveButtonEnable"
            )

            waitUntil({
                val playerName = PlayerRoster.element().all(By.className(playerCardStyles["header"]))
                    .first()
                    .text()

                (playerName == expectedName)
            }, 100, "PlayerConfig.waitForSave.nameIncluded")
    }

}

object PlayerCard {
    val playerCardStyles = loadStyles("player/PlayerCard")
    val playerLocator = By.className(playerCardStyles["player"])
    val headerLocator = By.className(playerCardStyles["header"])
    suspend fun getHeader() = element(headerLocator)
    suspend fun getPlayerElements() = all(playerLocator)
    val iconLocator = By.className(playerCardStyles["playerIcon"])
}

object PlayerRoster : StyleSyntax {
    override val styles = loadStyles("player/PlayerRoster")
    suspend fun getPlayerElements() = all(".${styles.className} .${playerCardStyles["player"]}")
    suspend fun getAddPlayerButton() = getting("addPlayerButton")
}
