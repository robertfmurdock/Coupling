package com.zegreatrob.coupling.e2e.test

import com.zegreatrob.coupling.e2e.test.ConfigForm.getDeleteButton
import com.zegreatrob.coupling.e2e.test.ConfigForm.getSaveButton
import com.zegreatrob.coupling.e2e.test.webdriverio.BrowserSyntax
import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.minassert.assertIsEqualTo
import kotlin.test.Test

@Suppress("unused")
class PartyConfigPageE2ETest {

    class GivenExistingParty {

        @Test
        fun canSaveEditsCorrectly() = sdkSetup(object : BrowserSyntax, SdkContext() {
            val party = buildParty()
            val expectedNewName = "Different name"
            val expectedDefaultBadgeName = "New Default Badge Name"
            val expectedAltBadgeName = "New Alt Badge Name"
            val expectedCallSignSelection = "true"
            val expectedBadgeSelection = "true"
            val page = PartyConfigPage
        }) {
            sdk.partyRepository.save(party)
            with(page) {
                goTo(party.id)

                getPartyNameInput().clearSetValue(expectedNewName)
                getCallSignCheckbox().click()
                getBadgeCheckbox().click()
                getDefaultBadgeNameInput().clearSetValue(expectedDefaultBadgeName)
                getAltBadgeNameInput().clearSetValue(expectedAltBadgeName)
                getDifferentBadgesOption().click()
            }
        } exercise {
            getSaveButton().click()
            PartyListPage.waitForPage()
            PartyConfigPage.goTo(party.id)
        } verify {
            with(page) {
                getPartyNameInput().attribute("value")
                    .assertIsEqualTo(expectedNewName)
                getCallSignCheckbox().attribute("checked")
                    .assertIsEqualTo(expectedCallSignSelection)
                getBadgeCheckbox().attribute("checked")
                    .assertIsEqualTo(expectedBadgeSelection)
                getDefaultBadgeNameInput().attribute("value")
                    .assertIsEqualTo(expectedDefaultBadgeName)
                getAltBadgeNameInput().attribute("value")
                    .assertIsEqualTo(expectedAltBadgeName)
                getCheckedOption().attribute("label")
                    .assertIsEqualTo("Prefer Different Badges (Beta)")
            }
        }

        @Test
        fun showsBasicInformation() = sdkSetup(object : SdkContext() {
            val party = buildParty().copy(email = "${randomInt()}-email")
            val page = PartyConfigPage
        }) {
            sdk.partyRepository.save(party)
        } exercise {
            PartyConfigPage.goTo(party.id)
        } verify {
            with(page) {
                getPartyNameInput().attribute("value")
                    .assertIsEqualTo(party.name)
                getPartyEmailInput().attribute("value")
                    .assertIsEqualTo(party.email)
            }
        }

        @Test
        fun canDeleteParty() = sdkSetup(object : SdkContext() {
            val party = buildParty()
        }) {
            sdk.partyRepository.save(party)
            PartyConfigPage.goTo(party.id)
        } exercise {
            getDeleteButton().click()
            PartyListPage.waitForPage()
        } verify {
            PartyListPage.partyCardElements
                .map { it.text() }
                .contains(party.name)
                .assertIsEqualTo(false)
        }

        companion object {
            private fun buildParty() = Party(
                id = "${randomInt()}-PartyConfigPageE2ETest-partyId".let(::PartyId),
                name = "${randomInt()}-PartyConfigPageE2ETest-name",
            )
        }
    }

    class NewParty {
        @Test
        fun idFieldShowsAndPersistsAsTextIsAdded() = e2eSetup(PartyConfigPage) {
            goToNew()
        } exercise {
            getPartyIdInput().clearSetValue("oopsie")
        } verify {
            getPartyIdInput().isDisplayed()
                .assertIsEqualTo(true)
        }

        @Test
        fun willDefaultPairingRuleToLongestTime() = e2eSetup(PartyConfigPage) exercise {
            goToNew()
        } verify {
            getCheckedOption().attribute("label")
                .assertIsEqualTo("Prefer Longest Time")
        }
    }
}
