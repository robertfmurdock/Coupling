package com.zegreatrob.coupling.server.e2e

import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.server.e2e.PlayerCard.playerCardStyles
import com.zegreatrob.coupling.server.e2e.external.protractor.*
import kotlinx.coroutines.await

object PlayerConfig : StyleSyntax {
    override val styles = loadStyles("player/PlayerConfig")

    val playerNameTextField = element(By.id("player-name"))
    val defaultBadgeOption = element(By.id("default-badge-option"))
    val altBadgeOption = element(By.id("alt-badge-option"))
    val adjectiveTextInput = element(By.id("adjective-input"))
    val nounTextInput = element(By.id("noun-input"))

    suspend fun goTo(tribeId: TribeId, playerId: String?) {
        setLocation("/${tribeId.value}/player/${playerId}")
        waitForPage()
    }

    suspend fun goToNew(tribeId: TribeId) {
        setLocation("/${tribeId.value}/player/new")
        waitForPage()
    }

    suspend fun waitForPage() {
        element.waitToBePresent()
    }

    suspend fun waitForSaveToComplete(name: String?) {
        browser.wait(
            { ConfigForm.saveButton.isEnabled().then({ it }, { false }) },
            waitToBePresentDuration,
            "PlayerConfig.waitForSaveButtonDisable"
        ).await()

        browser.wait({
            all(By.css(".${styles["playerRoster"]} .${playerCardStyles["header"]}"))
                .first()
                .getText()
                .then { it == name }
        }, 100, "PlayerConfig.waitForSave.nameIncluded").await()
    }

}

object PlayerCard {
    val playerCardStyles = loadStyles("player/PlayerCard")
    val playerLocator = By.className(playerCardStyles["player"])
    val headerLocator = By.className(playerCardStyles["header"])
    val header = element(headerLocator)
    val playerElements = all(playerLocator)
    val iconLocator: ProtractorBy = By.className(playerCardStyles["playerIcon"])
}

object PlayerRoster : StyleSyntax {
    override val styles = loadStyles("player/PlayerRoster")
    val playerElements = all(By.css(".${styles.className} .${playerCardStyles["player"]}"))
    val addPlayerButton by getting()
}
