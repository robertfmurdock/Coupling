package com.zegreatrob.coupling.e2e.test

import com.zegreatrob.coupling.e2e.test.ConfigForm.getDeleteButton
import com.zegreatrob.coupling.e2e.test.ConfigForm.getSaveButton
import com.zegreatrob.coupling.e2e.test.CouplingLogin.sdkProvider
import com.zegreatrob.coupling.e2e.test.PinListPage.element
import com.zegreatrob.coupling.e2e.test.webdriverio.waitToBePresentDuration
import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.party.with
import com.zegreatrob.coupling.model.pin.Pin
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

        private val partySetup = e2eSetup.extend(beforeAll = {
            val party = Party(PartyId("${randomInt()}-PinConfigE2ETest-test"))
            val sdk = sdkProvider.await().apply {
                party.save()
            }
            sdk to party
        })
    }

    @Test
    fun whenThePinIsNewAndTheAddButtonIsPressedThePinIsSaved() = partySetup.with(
        object : PartyContext() {
            val newPinName = "Excellent pin name${randomInt()}"
        }.attachParty()
    ) {
        with(PinConfigPage) {
            party.id.goToNew()
            getNameTextField().setValue(newPinName)
        }
    } exercise {
        getSaveButton().click()
    } verify {
        with(PinConfigPage) {
            waitForPinNameToAppear(newPinName, party.id)
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
        "PinConfigPage.waitForPinNameToAppear in party ${id.value}"
    )

    class WhenThePinExists {

        @Test
        fun attributesAreShownOnConfig() = partySetup.with(
            object : PartyContext() {
                val pin = randomPin()
            }.attachParty()
        ) {
            sdk.pinRepository.save(party.id.with(pin))
        } exercise {
            PinConfigPage.goTo(party.id, pin.id)
        } verify {
            with(PinConfigPage) {
                getNameTextField().attribute("value")
                    .assertIsEqualTo(pin.name)
                getIconTextField().attribute("value")
                    .assertIsEqualTo(pin.icon)
            }
        }

        @Test
        fun clickingDeleteWillRemovePinFromPinList() = partySetup.with(
            object : PartyContext() {
                val pin = randomPin()
            }.attachParty()
        ) {
            sdk.pinRepository.save(party.id.with(pin))
            PinConfigPage.goTo(party.id, pin.id)
        } exercise {
            getDeleteButton().click()
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
