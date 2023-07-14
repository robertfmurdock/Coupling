package com.zegreatrob.coupling.e2e.test

import com.zegreatrob.coupling.action.party.SavePartyCommand
import com.zegreatrob.coupling.action.pin.SavePinCommand
import com.zegreatrob.coupling.action.pin.fire
import com.zegreatrob.coupling.e2e.test.ConfigForm.getDeleteButton
import com.zegreatrob.coupling.e2e.test.ConfigForm.getSaveButton
import com.zegreatrob.coupling.e2e.test.CouplingLogin.sdk
import com.zegreatrob.coupling.e2e.test.webdriverio.waitToBePresentDuration
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.minassert.assertContains
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.wrapper.wdio.WebdriverBrowser
import com.zegreatrob.wrapper.wdio.WebdriverElementArray
import kotlin.random.Random
import kotlin.test.Test

class PinConfigE2ETest {

    companion object {

        private fun randomPin(nameExt: String = "") = Pin(
            id = "${randomInt()}-pin",
            icon = "icon-${randomInt()}",
            name = "name-${randomInt()}-$nameExt",
        )

        private val partySetup = e2eSetup.extend(beforeAll = {
            val party = PartyDetails(PartyId("${randomInt()}-PinConfigE2ETest-test"))
            val sdk = sdk.await().apply {
                fire(SavePartyCommand(party))
            }
            sdk to party
        })
    }

    @Test
    fun whenThePinIsNewAndTheAddButtonIsPressedThePinIsSaved() = partySetup.with(
        object : PartyContext() {
            val newPinName = "Excellent pin name${randomInt()}"
        }.attachParty(),
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
        id: PartyId,
    ) = WebdriverBrowser.waitUntil(
        { pinBagPinNames().contains(newPinName) },
        waitToBePresentDuration,
        "PinConfigPage.waitForPinNameToAppear in party ${id.value}",
    )

    class WhenThePinExists {

        @Test
        fun attributesAreShownOnConfig() = partySetup.with(
            object : PartyContext() {
                val pin = randomPin()
            }.attachParty(),
        ) {
            fire(sdk, SavePinCommand(party.id, pin))
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
            }.attachParty(),
        ) {
            fire(sdk, SavePinCommand(party.id, pin))
            PinConfigPage.goTo(party.id, pin.id)
        } exercise {
            getDeleteButton().click()
            WebdriverBrowser.acceptAlert()

            PinListPage.waitForLoad()
        } verify {
            WebdriverElementArray(".pin-name")
                .map { it.text() }
                .contains(pin.name)
                .assertIsEqualTo(false)
        }
    }
}

fun randomInt() = Random.nextInt()
