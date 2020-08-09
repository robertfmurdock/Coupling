package com.zegreatrob.coupling.server.e2e

import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.server.e2e.ConfigForm.deleteButton
import com.zegreatrob.coupling.server.e2e.ConfigForm.saveButton
import com.zegreatrob.coupling.server.e2e.external.webdriverio.BrowserSyntax
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.invoke
import kotlin.test.Test

@Suppress("unused")
class TribeConfigPageE2ETest {

    class GivenExistingTribe {

        @Test
        fun canSaveEditsCorrectly() = sdkSetup(object : BrowserSyntax, SdkContext() {
            val tribe = buildTribe()
            val expectedNewName = "Different name"
            val expectedDefaultBadgeName = "New Default Badge Name"
            val expectedAltBadgeName = "New Alt Badge Name"
            val expectedCallSignSelection = "true"
            val expectedBadgeSelection = "true"
            val page = TribeConfigPage
        }) {
            sdk.save(tribe)
            with(page) {
                goTo(tribe.id)

                getTribeNameInput().clearSetValue(expectedNewName)
                getCallSignCheckbox().click()
                getBadgeCheckbox().click()
                getDefaultBadgeNameInput().clearSetValue(expectedDefaultBadgeName)
                getAltBadgeNameInput().clearSetValue(expectedAltBadgeName)
                getDifferentBadgesOption().click()
            }
        } exercise {
            saveButton.click()
            TribeListPage.waitForPage()
            page.goTo(tribe.id)
        } verify {
            with(page) {
                getTribeNameInput().attribute("value")
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
            val tribe = buildTribe().copy(email = "${randomInt()}-email")
            val page = TribeConfigPage
        }) {
            sdk.save(tribe)
        } exercise {
            page.goTo(tribe.id)
        } verify {
            with(page) {
                getTribeNameInput().attribute("value")
                    .assertIsEqualTo(tribe.name)
                getTribeEmailInput().attribute("value")
                    .assertIsEqualTo(tribe.email)
            }
        }

        @Test
        fun canDeleteTribe() = sdkSetup(object : SdkContext() {
            val tribe = buildTribe()
        }) {
            sdk.save(tribe)
            TribeConfigPage.goTo(tribe.id)
        } exercise {
            deleteButton.click()
            TribeListPage.waitForPage()
        } verify {
            TribeListPage.tribeCardElements
                .map { it.text() }
                .contains(tribe.name)
                .assertIsEqualTo(false)
        }

        companion object {
            private fun buildTribe() = Tribe(
                id = "${randomInt()}-TribeConfigPageE2ETest-tribeId".let(::TribeId),
                name = "${randomInt()}-TribeConfigPageE2ETest-name"
            )
        }
    }

    class NewTribe {
        @Test
        fun idFieldShowsAndPersistsAsTextIsAdded() = e2eSetup(TribeConfigPage) {
            goToNew()
        } exercise {
            getTribeIdInput().clearSetValue("oopsie")
        } verify {
            getTribeIdInput().isDisplayed()
                .assertIsEqualTo(true)
        }

        @Test
        fun willDefaultPairingRuleToLongestTime() = e2eSetup(TribeConfigPage) exercise {
            goToNew()
        } verify {
            getCheckedOption().attribute("label")
                .assertIsEqualTo("Prefer Longest Time")
        }
    }

}
