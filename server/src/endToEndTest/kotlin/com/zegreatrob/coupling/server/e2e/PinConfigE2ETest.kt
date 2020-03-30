package com.zegreatrob.coupling.server.e2e

import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.model.tribe.with
import com.zegreatrob.coupling.server.e2e.CouplingLogin.loginProvider
import com.zegreatrob.coupling.server.e2e.CouplingLogin.sdkProvider
import com.zegreatrob.coupling.server.e2e.external.protractor.*
import com.zegreatrob.minassert.assertContains
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.setupAsync
import com.zegreatrob.testmints.async.testAsync
import kotlinx.coroutines.*
import kotlin.random.Random
import kotlin.test.Test

class PinConfigE2ETest {

    @Test
    fun whenThePinIsNewAndTheAddButtonIsPressedThePinIsSaved() = testWithTribe { tribe ->
        setupAsync(object {
            val newPinName = "Excellent pin name${randomInt()}"
        }) {
            with(PinConfigPage) {
                tribe.id.goToNew()
                nameTextField.performClear()
                nameTextField.performSendKeys(newPinName)
            }
        } exerciseAsync {
            with(PinConfigPage) {
                saveButton.performClick()
            }
        } verifyAsync {
            with(PinConfigPage) {
                waitForPinNameToAppear(newPinName)
                pinBagPinNames()
                    .assertContains(newPinName)
            }
        }
    }

    private suspend fun PinConfigPage.waitForPinNameToAppear(newPinName: String) = browser.wait({
        val scope = MainScope()
        try {
            scope.async { pinBagPinNames().contains(newPinName) }
        } catch (bad: Throwable) {
            scope.async { false }
        }.asPromise()
    }, 2000, "PinConfigPage.waitForLoad").await()


    class WhenThePinExists {

        @Test
        fun attributesAreShownOnConfig() = testWithTribe { tribe ->
            val sdk = sdkProvider.await()
            setupAsync(object {
                val pin = randomPin()
            }) {
                sdk.save(tribe.id.with(pin))
            } exerciseAsync {
                PinConfigPage.goTo(tribe.id, pin._id)
            } verifyAsync {
                with(PinConfigPage) {
                    nameTextField.getAttribute("value").await()
                        .assertIsEqualTo(pin.name)
                    iconTextField.getAttribute("value").await()
                        .assertIsEqualTo(pin.icon)
                }
            }
        }

        @Test
        fun clickingDeleteWillRemovePinFromPinList() = testWithTribe { tribe ->
            val sdk = sdkProvider.await()
            setupAsync(object {
                val pin = randomPin("delete")
            }) {
                sdk.save(tribe.id.with(pin))
                PinConfigPage.goTo(tribe.id, pin._id)
            } exerciseAsync {
                PinConfigPage.deleteButton.performClick()
                browser.switchTo().alert().await()
                    .accept().await()

                PinListPage.waitForLoad()
            } verifyAsync {
                PinListPage.page.all(By.className("pin-name"))
                    .map { it.getText() }
                    .await()
                    .toList()
                    .contains(pin.name)
                    .assertIsEqualTo(false)
            }
        }

    }

    companion object {

        private fun randomPin(nameExt: String = "") = Pin(
            _id = "${randomInt()}-pin",
            icon = "icon-${randomInt()}",
            name = "name-${randomInt()}-$nameExt"
        )

        private fun testWithTribe(handler: suspend (tribe: Tribe) -> Unit) = testAsync {
            val tribe = tribeProvider.await()
            loginProvider.await()

            try {
                handler(tribe)
            } finally {
                checkLogs()
            }
        }

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