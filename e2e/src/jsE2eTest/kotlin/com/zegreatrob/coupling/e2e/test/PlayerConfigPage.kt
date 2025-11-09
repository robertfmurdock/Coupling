package com.zegreatrob.coupling.e2e.test

import com.zegreatrob.coupling.e2e.test.ConfigForm.saveButton
import com.zegreatrob.coupling.e2e.test.webdriverio.BrowserSyntax
import com.zegreatrob.coupling.e2e.test.webdriverio.WAIT_TO_BE_PRESENT_DURATION
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.player.PlayerId
import com.zegreatrob.wrapper.wdio.WebdriverBrowser
import com.zegreatrob.wrapper.wdio.WebdriverElementArray
import com.zegreatrob.wrapper.wdio.testing.library.ByRole
import com.zegreatrob.wrapper.wdio.testing.library.RoleOptions
import com.zegreatrob.wrapper.wdio.testing.library.TestingLibraryBrowser

object PlayerConfigPage : BrowserSyntax, ByRole by TestingLibraryBrowser {

    suspend fun playerNameTextField() = getByRole("textbox", RoleOptions(name = "Name"))
    suspend fun defaultBadgeOption() = queryByRole("option", RoleOptions(name = "Default Badge Option"))
    suspend fun altBadgeOption() = queryByRole("option", RoleOptions(name = "Alt Badge Option"))
    suspend fun adjectiveTextInput() = queryByRole("combobox", RoleOptions(name = "Call-Sign Adjective"))
    suspend fun nounTextInput() = queryByRole("combobox", RoleOptions(name = "Call-Sign Noun"))

    suspend fun goTo(partyId: PartyId, playerId: PlayerId) {
        WebdriverBrowser.setLocation("/${partyId.value}/player/${playerId.value}")
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
            { saveButton().isEnabled() },
            WAIT_TO_BE_PRESENT_DURATION,
            "PlayerConfig.waitForSaveButtonEnable",
        )

        WebdriverBrowser.waitUntil({
            PlayerRoster.element().all(PlayerCard.PLAYER_LOCATOR)
                .map { it.text() }
                .contains(expectedName)
        }, 100, "PlayerConfig.waitForSave.nameIncluded name=$expectedName")
    }
}

object PlayerCard : BrowserSyntax {
    const val PLAYER_LOCATOR = "[data-player-id]"
    val playerElements get() = WebdriverElementArray(PLAYER_LOCATOR)
    const val ICON_LOCATOR = "[alt=player-icon]"
}

object PlayerRoster {
    suspend fun element(rosterLabel: String = "Players") = TestingLibraryBrowser.getByText(rosterLabel).parentElement()

    suspend fun getPlayerElements(playerRosterLabel: String) = TestingLibraryBrowser.getByText(playerRosterLabel)
        .parentElement()
        .all(PlayerCard.playerElements.selector)

    suspend fun getAddPlayerButton() = TestingLibraryBrowser.queryByRole("button", RoleOptions("Add Player!"))
}
