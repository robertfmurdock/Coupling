package com.zegreatrob.coupling.e2e.test

import com.zegreatrob.coupling.e2e.test.ConfigForm.deleteButton
import com.zegreatrob.coupling.e2e.test.ConfigForm.saveButton
import com.zegreatrob.coupling.e2e.test.CouplingLogin.sdkProvider
import com.zegreatrob.coupling.e2e.test.PinListPage.element
import com.zegreatrob.coupling.e2e.test.webdriverio.waitToBePresentDuration
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.party.with
import com.zegreatrob.minassert.assertContains
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.wrapper.wdio.By
import com.zegreatrob.wrapper.wdio.WebdriverBrowser
import kotlin.random.Random
import kotlin.test.Test

class PinConfigE2ETest {

    companion object {

        private fun randomPin(nameExt: String = "") = Pin(
            id = "${randomInt()}-pin",
            icon = "icon-${randomInt()}",
            name = "name-${randomInt()}-$nameExt"
        )

        private val tribeSetup = e2eSetup.extend(beforeAll = {
            val tribe = Party(PartyId("${randomInt()}-PinConfigE2ETest-test"))
            val sdk = sdkProvider.await().apply {
                tribe.save()
            }
            sdk to tribe
        })
    }

    @Test
    fun whenThePinIsNewAndTheAddButtonIsPressedThePinIsSaved() = tribeSetup.with(object : TribeContext() {
        val newPinName = "Excellent pin name${randomInt()}"
    }.attachParty()) {
        with(PinConfigPage) {
            tribe.id.goToNew()
            getNameTextField().setValue(newPinName)
        }
    } exercise {
        saveButton.click()
    } verify {
        with(PinConfigPage) {
            waitForPinNameToAppear(newPinName, tribe.id)
            pinBagPinNames()
                .assertContains(newPinName)
        }
    }

    private suspend fun PinConfigPage.waitForPinNameToAppear(
        newPinName: String,
        id: PartyId
    ) = WebdriverBrowser.waitUntil(
        { pinBagPinNames().contains(newPinName) },
        waitToBePresentDuration,
        "PinConfigPage.waitForPinNameToAppear in tribe ${id.value}"
    )

    class WhenThePinExists {

        @Test
        fun attributesAreShownOnConfig() = tribeSetup.with(object : TribeContext() {
            val pin = randomPin()
        }.attachParty()) {
            sdk.pinRepository.save(tribe.id.with(pin))
        } exercise {
            PinConfigPage.goTo(tribe.id, pin.id)
        } verify {
            with(PinConfigPage) {
                getNameTextField().attribute("value")
                    .assertIsEqualTo(pin.name)
                getIconTextField().attribute("value")
                    .assertIsEqualTo(pin.icon)
            }
        }

        @Test
        fun clickingDeleteWillRemovePinFromPinList() = tribeSetup.with(object : TribeContext() {
            val pin = randomPin()
        }.attachParty()) {
            sdk.pinRepository.save(tribe.id.with(pin))
            PinConfigPage.goTo(tribe.id, pin.id)
        } exercise {
            deleteButton.click()
            WebdriverBrowser.acceptAlert()

            PinListPage.waitForLoad()
        } verify {
            element().all(By.className("pin-name"))
                .map { it.text() }
                .contains(pin.name)
                .assertIsEqualTo(false)
        }

    }
}

fun randomInt() = Random.nextInt()
