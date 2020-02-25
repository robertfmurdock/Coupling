package com.zegreatrob.coupling.server.e2e

import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.sdk.Sdk
import com.zegreatrob.coupling.server.e2e.CouplingLogin.sdkProvider
import com.zegreatrob.coupling.server.e2e.external.protractor.ProtractorSyntax
import com.zegreatrob.coupling.server.e2e.external.protractor.performClearSendKeys
import com.zegreatrob.coupling.server.e2e.external.protractor.performClick
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.setupAsync
import com.zegreatrob.testmints.async.testAsync
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.await
import kotlin.test.Test

@Suppress("unused")
class TribeConfigPageE2ETest {

    class GivenExistingTribe {

        @Test
        fun canSaveEditsCorrectly() = testWithSdk { sdk ->
            val tribe = buildTribe()
            sdk.save(tribe)
            with(TribeConfigPage) {
                setupAsync(object : ProtractorSyntax {
                    val expectedNewName = "Different name"
                    val expectedDefaultBadgeName = "New Default Badge Name"
                    val expectedAltBadgeName = "New Alt Badge Name"
                    val expectedCallSignSelection = "true"
                    val expectedBadgeSelection = "true"
                }) {
                    goTo(tribe.id)

                    tribeNameInput.performClearSendKeys(expectedNewName)
                    callSignCheckbox.performClick()
                    badgeCheckbox.performClick()
                    defaultBadgeNameInput.performClearSendKeys(expectedDefaultBadgeName)
                    altBadgeNameInput.performClearSendKeys(expectedAltBadgeName)
                    differentBadgesOption.performClick()
                } exerciseAsync {
                    saveButton.performClick()
                    TribeListPage.waitForPage()
                    goTo(tribe.id)
                } verifyAsync {
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
        }

        @Test
        fun showsBasicInformation() = testWithSdk { sdk ->
            val tribe = buildTribe().copy(email = "${randomInt()}-email")
            sdk.save(tribe)
            setupAsync(TribeConfigPage) exerciseAsync {
                goTo(tribe.id)
            } verifyAsync {
                tribeNameInput.getAttribute("value").await()
                    .assertIsEqualTo(tribe.name)
                tribeEmailInput.getAttribute("value").await()
                    .assertIsEqualTo(tribe.email)
            }
        }

        @Test
        fun canDeleteTribe() = testWithSdk { sdk ->
            val tribe = buildTribe()
            sdk.save(tribe)
            setupAsync(object {}) {
                TribeConfigPage.goTo(tribe.id)
            } exerciseAsync {
                TribeConfigPage.deleteButton.performClick()
                TribeListPage.waitForPage()
            } verifyAsync {
                TribeListPage.tribeCardElements
                    .map { it.getText() }
                    .await()
                    .contains(tribe.name)
                    .assertIsEqualTo(false)
            }
        }

        companion object {
            fun testWithSdk(handler: suspend CoroutineScope.(Sdk) -> Unit) = testAsync {
                CouplingLogin.login.await()
                handler(sdkProvider.await())
            }

            private fun buildTribe() = Tribe(
                id = "${randomInt()}-TribeConfigPageE2ETest-tribeId".let(::TribeId),
                name = "${randomInt()}-TribeConfigPageE2ETest-name"
            )
        }
    }

    class NewTribe {
        @Test
        fun idFieldShowsAndPersistsAsTextIsAdded() = testAsync {
            CouplingLogin.login.await()
            setupAsync(TribeConfigPage) {
                goToNew()
            } exerciseAsync {
                tribeIdInput.performClearSendKeys("oopsie")
            } verifyAsync {
                tribeIdInput.isDisplayed().await()
                    .assertIsEqualTo(true)
            }
        }

        @Test
        fun willDefaultPairingRuleToLongestTime() = testAsync {
            CouplingLogin.login.await()
            setupAsync(TribeConfigPage) exerciseAsync {
                goToNew()
            } verifyAsync {
                checkedOption.getAttribute("label").await()
                    .assertIsEqualTo("Prefer Longest Time")
            }
        }
    }

}
