package com.zegreatrob.coupling.server.e2e

import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.server.e2e.external.protractor.ProtractorSyntax
import com.zegreatrob.coupling.server.e2e.external.protractor.performClearSendKeys
import com.zegreatrob.coupling.server.e2e.external.protractor.performClick
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.invoke
import kotlinx.coroutines.await
import kotlin.test.Test

@Suppress("unused")
class TribeConfigPageE2ETest {

    class GivenExistingTribe {

        @Test
        fun canSaveEditsCorrectly() = sdkSetup(object : ProtractorSyntax, SdkContext() {
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

                tribeNameInput.performClearSendKeys(expectedNewName)
                callSignCheckbox.performClick()
                badgeCheckbox.performClick()
                defaultBadgeNameInput.performClearSendKeys(expectedDefaultBadgeName)
                altBadgeNameInput.performClearSendKeys(expectedAltBadgeName)
                differentBadgesOption.performClick()
            }
        } exercise {
            ConfigForm.saveButton.performClick()
            TribeListPage.waitForPage()
            page.goTo(tribe.id)
        } verify {
            with(page) {
                tribeNameInput.getAttribute("value").await()
                    .assertIsEqualTo(expectedNewName)
                callSignCheckbox.getAttribute("checked").await()
                    .assertIsEqualTo(expectedCallSignSelection)
                badgeCheckbox.getAttribute("checked").await()
                    .assertIsEqualTo(expectedBadgeSelection)
                defaultBadgeNameInput.getAttribute("value").await()
                    .assertIsEqualTo(expectedDefaultBadgeName)
                altBadgeNameInput.getAttribute("value").await()
                    .assertIsEqualTo(expectedAltBadgeName)
                checkedOption.getAttribute("label").await()
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
                tribeNameInput.getAttribute("value").await()
                    .assertIsEqualTo(tribe.name)
                tribeEmailInput.getAttribute("value").await()
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
            ConfigForm.deleteButton.performClick()
            TribeListPage.waitForPage()
        } verify {
            TribeListPage.tribeCardElements
                .map { it.getText() }
                .await()
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
            tribeIdInput.performClearSendKeys("oopsie")
        } verify {
            tribeIdInput.isDisplayed().await()
                .assertIsEqualTo(true)
        }

        @Test
        fun willDefaultPairingRuleToLongestTime() = e2eSetup(TribeConfigPage) exercise {
            goToNew()
        } verify {
            checkedOption.getAttribute("label").await()
                .assertIsEqualTo("Prefer Longest Time")
        }
    }

}
