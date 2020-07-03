package com.zegreatrob.coupling.server.e2e

import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.model.tribe.with
import com.zegreatrob.coupling.server.e2e.CouplingLogin.sdkProvider
import com.zegreatrob.coupling.server.e2e.external.protractor.*
import com.zegreatrob.minassert.assertContains
import com.zegreatrob.minassert.assertIsEqualTo
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.asPromise
import kotlinx.coroutines.async
import kotlinx.coroutines.await
import kotlin.random.Random
import kotlin.test.Test

class PinConfigE2ETest {

    companion object {

        private fun randomPin(nameExt: String = "") = Pin(
            _id = "${randomInt()}-pin",
            icon = "icon-${randomInt()}",
            name = "name-${randomInt()}-$nameExt"
        )

        private val tribeSetup = e2eSetup.extend(beforeAll = {
            val sdk = sdkProvider.await()
            val tribe = Tribe(TribeId("${randomInt()}-PinConfigE2ETest-test"))
            sdk.save(tribe)
            sdk to tribe
        })
    }

    @Test
    fun whenThePinIsNewAndTheAddButtonIsPressedThePinIsSaved() = tribeSetup(object : TribeContext() {
        val newPinName = "Excellent pin name${randomInt()}"
    }.attachTribe()) {
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
        fun attributesAreShownOnConfig() = tribeSetup(object : TribeContext() {
            val pin = randomPin()
        }.attachTribe()) {
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
        fun clickingDeleteWillRemovePinFromPinList() = tribeSetup(object : TribeContext() {
            val pin = randomPin()
        }.attachTribe()) {
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
}

fun randomInt() = Random.nextInt()

suspend fun checkLogs() {
    if (browser.getCapabilities().await()["browserName"] != "firefox") {
        val browserLog = getBrowserLogs()
        browserLog.toList()
            .assertIsEqualTo(emptyList(), JSON.stringify(browserLog))
    }
}

suspend fun getBrowserLogs() = browser.manage().logs()["browser"].await()