package com.zegreatrob.coupling.e2e.test

import com.zegreatrob.coupling.e2e.test.ConfigForm.getSaveButton
import com.zegreatrob.coupling.e2e.test.webdriverio.BrowserSyntax
import com.zegreatrob.coupling.e2e.test.webdriverio.waitToBePresentDuration
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.wrapper.wdio.WebdriverBrowser
import com.zegreatrob.wrapper.wdio.WebdriverElementArray
import com.zegreatrob.wrapper.wdio.testing.library.ByRole
import com.zegreatrob.wrapper.wdio.testing.library.RoleOptions
import com.zegreatrob.wrapper.wdio.testing.library.TestingLibraryBrowser

object PlayerConfigPage : BrowserSyntax, ByRole by TestingLibraryBrowser {

    suspend fun playerNameTextField() = getByRole("combobox", RoleOptions(name = "Name"))
    suspend fun defaultBadgeOption() = queryByRole("option", RoleOptions(name = "Default Badge Option"))
    suspend fun altBadgeOption() = queryByRole("option", RoleOptions(name = "Alt Badge Option"))
    suspend fun adjectiveTextInput() = queryByRole("combobox", RoleOptions(name = "Call-Sign Adjective"))
    suspend fun nounTextInput() = queryByRole("combobox", RoleOptions(name = "Call-Sign Noun"))

    suspend fun goTo(partyId: PartyId, playerId: String?) {
        WebdriverBrowser.setLocation("/${partyId.value}/player/$playerId")
        waitForPage()
    }

    suspend fun goToNew(partyId: PartyId) {
        WebdriverBrowser.setLocation("/${partyId.value}/player/new")
        waitForPage()
    }

    suspend fun waitForPage() {
        WebdriverBrowser.waitUntil({ TestingLibraryBrowser.getByText("Player Configuration").isDisplayed() })
    }

    suspend fun waitForSaveToComplete(expectedName: String?) {
        WebdriverBrowser.waitUntil(
            { getSaveButton().isEnabled() },
            waitToBePresentDuration,
            "PlayerConfig.waitForSaveButtonEnable"
        )

        WebdriverBrowser.waitUntil({
            val playerName = PlayerRoster.element().all(PlayerCard.playerLocator)
                .first()
                .text()
            (playerName == expectedName)
        }, 100, "PlayerConfig.waitForSave.nameIncluded")
    }
}

object PlayerCard : BrowserSyntax {
    val playerLocator = "[data-player-id]"
    val playerElements get() = WebdriverElementArray(playerLocator)
    val iconLocator = "[alt=player-icon]"
}

object PlayerRoster : StyleSyntax {
    override val styles = loadStyles("player/PlayerRoster")
    val playerElements = styles.element.all(PlayerCard.playerElements.selector)
    suspend fun getAddPlayerButton() = TestingLibraryBrowser.queryByRole("button", RoleOptions("Add a new player!"))
}
