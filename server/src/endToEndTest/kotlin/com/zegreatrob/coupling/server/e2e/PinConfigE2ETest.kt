package com.zegreatrob.coupling.server.e2e

import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.model.tribe.with
import com.zegreatrob.coupling.server.e2e.CouplingLogin.sdkProvider
import com.zegreatrob.coupling.server.e2e.external.protractor.*
import com.zegreatrob.minassert.assertContains
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.invoke
import kotlinx.coroutines.*
import kotlin.random.Random
import kotlin.test.Test

class PinConfigE2ETest {

    @Test
    fun whenThePinIsNewAndTheAddButtonIsPressedThePinIsSaved() = testWithTribe(object : TribeContext() {
        val newPinName = "Excellent pin name${randomInt()}"
    }) {
        with(PinConfigPage) {
            tribe.id.goToNew()
            nameTextField.performClear()
            nameTextField.performSendKeys(newPinName)
        }
    } exercise {
        with(PinConfigPage) {
            saveButton.performClick()
        }
    } verify {
        with(PinConfigPage) {
            waitForPinNameToAppear(newPinName)
            pinBagPinNames()
                .assertContains(newPinName)
        }
    }

    private suspend fun PinConfigPage.waitForPinNameToAppear(newPinName: String) = browser.wait({
        MainScope().async { pinBagPinNames().contains(newPinName) }
            .asPromise().then({ it }) { false }
    }, 2000, "PinConfigPage.waitForPinNameToAppear").await()

    class WhenThePinExists {

        @Test
        fun attributesAreShownOnConfig() = testWithTribe(object : TribeContext() {
            val pin = randomPin()
        }) {
            sdk.save(tribe.id.with(pin))
        } exercise {
            PinConfigPage.goTo(tribe.id, pin._id)
        } verify {
            with(PinConfigPage) {
                nameTextField.getAttribute("value").await()
                    .assertIsEqualTo(pin.name)
                iconTextField.getAttribute("value").await()
                    .assertIsEqualTo(pin.icon)
            }
        }

        @Test
        fun clickingDeleteWillRemovePinFromPinList() = testWithTribe(object : TribeContext() {
            val pin = randomPin()
        }) {
            sdk.save(tribe.id.with(pin))
            PinConfigPage.goTo(tribe.id, pin._id)
        } exercise {
            PinConfigPage.deleteButton.performClick()
            browser.switchTo().alert().await()
                .accept().await()

            PinListPage.waitForLoad()
        } verify {
            PinListPage.page.all(By.className("pin-name"))
                .map { it.getText() }
                .await()
                .toList()
                .contains(pin.name)
                .assertIsEqualTo(false)
        }

    }

    companion object {

        private fun randomPin(nameExt: String = "") = Pin(
            _id = "${randomInt()}-pin",
            icon = "icon-${randomInt()}",
            name = "name-${randomInt()}-$nameExt"
        )

        fun <C : TribeContext> testWithTribe(
            context: C,
            additionalActions: suspend C.() -> Unit
        ) = e2eSetup(
            contextProvider = {
                context.attachTribe(
                    tribeProvider.await(),
                    sdkProvider.await()
                )
            }, additionalActions = additionalActions
        )

        private val tribeProvider by lazy {
            GlobalScope.async {
                val sdk = sdkProvider.await()
                Tribe(TribeId("${randomInt()}-PinConfigE2ETest-test"))
                    .also { sdk.save(it) }
            }
        }
    }
}

fun randomInt() = Random.nextInt()

suspend fun checkLogs() {
    if (browser.getCapabilities().await()["browserName"] != "firefox") {
        val browserLog = browser.manage().logs().get("browser").await()
        browserLog.toList()
            .assertIsEqualTo(emptyList(), JSON.stringify(browserLog))
    }
}