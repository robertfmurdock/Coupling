package com.zegreatrob.coupling.sdk

import com.benasher44.uuid.uuid4
import com.zegreatrob.coupling.action.NotFoundResult
import com.zegreatrob.coupling.action.party.DeletePartyCommand
import com.zegreatrob.coupling.action.party.SavePartyCommand
import com.zegreatrob.coupling.action.pin.DeletePinCommand
import com.zegreatrob.coupling.action.pin.SavePinCommand
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.pin.pin
import com.zegreatrob.coupling.repository.validation.verifyWithWait
import com.zegreatrob.coupling.stubmodel.stubParty
import com.zegreatrob.coupling.stubmodel.stubPin
import com.zegreatrob.minassert.assertContains
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.minassert.assertIsNotEqualTo
import com.zegreatrob.testmints.async.AsyncMints.asyncSetup
import com.zegreatrob.testmints.async.AsyncMints.asyncTestTemplate
import kotlin.test.Test

class SdkPinTest {

    private val partySetup = asyncTestTemplate(
        sharedSetup = suspend {
            val sdk = sdk()
            object : BarebonesSdk by sdk {
                val party = stubParty()
            }.apply { sdk.perform(SavePartyCommand(party)) }
        },
        sharedTeardown = { it.perform(DeletePartyCommand(it.party.id)) },
    )

    @Test
    fun canSaveAndGetPins() = partySetup.with(
        {
            object : BarebonesSdk by it {
                val party = it.party
                val pins = listOf(
                    stubPin(),
                    stubPin(),
                    stubPin(),
                )
            }
        },
    ) exercise {
        pins.forEach { perform(SavePinCommand(party.id, it)) }
    } verifyWithWait {
        getPins(party.id)
            .map { it.data.pin }
            .assertIsEqualTo(pins)
    }

    @Test
    fun deleteWillFailWhenPinDoesNotExist() = partySetup() exercise {
        perform(DeletePinCommand(party.id, "${uuid4()}"))
    } verify { result ->
        result.assertIsEqualTo(NotFoundResult("Pin"))
    }

    @Test
    fun givenNoPinsWillReturnEmptyList() = partySetup() exercise {
        getPins(party.id)
    } verify { result ->
        result.assertIsEqualTo(emptyList())
    }

    @Test
    fun saveThenDeleteWillNotShowThatPin() = partySetup.with({
        object : BarebonesSdk by it {
            val party = it.party
            val pins = listOf(
                stubPin(),
                stubPin(),
                stubPin(),
            )
        }
    }) exercise {
        pins.forEach { perform(SavePinCommand(party.id, it)) }
        perform(DeletePinCommand(party.id, this.pins[1].id!!))
    } verifyWithWait {
        getPins(party.id).map { it.data.pin }
            .assertContains(this.pins[0])
            .assertContains(this.pins[2])
            .size
            .assertIsEqualTo(2)
    }

    @Test
    fun saveWorksWithNullableValuesAndAssignsIds() = partySetup.with({
        object : BarebonesSdk by it {
            val partyId = it.party.id
            val pin = Pin(
                id = null,
                name = "",
                icon = "",
            )
        }
    }) exercise {
        perform(SavePinCommand(partyId, pin))
    } verifyWithWait {
        getPins(this.partyId).map { it.data.pin }
            .also { it.assertHasIds() }
            .map { it.copy(id = null) }
            .assertIsEqualTo(listOf(this.pin))
    }

    private fun List<Pin>.assertHasIds() {
        forEach { pin -> pin.id.assertIsNotEqualTo(null) }
    }

    @Test
    fun givenNoAuthGetIsNotAllowed() = asyncSetup.with({
        val sdk = sdk()
        val otherSdk = altAuthorizedSdkDeferred.await()
        object {
            val otherParty = stubParty()
            val sdk = sdk
            val otherSdk = otherSdk
        }
    }) {
        otherSdk.perform(SavePartyCommand(otherParty))
        otherSdk.perform(SavePinCommand(otherParty.id, stubPin()))
    } exercise {
        sdk.getPins(otherParty.id)
    } verifyAnd { result ->
        result.assertIsEqualTo(emptyList())
    } teardown {
        otherSdk.perform(DeletePartyCommand(otherParty.id))
    }

    @Test
    fun savedPinsIncludeModificationDateAndUsername() = asyncSetup(object {
        val party = stubParty()
        val pin = stubPin()
        lateinit var sdk: BarebonesSdk
    }) {
        sdk = sdk()
        sdk.perform(SavePartyCommand(party))
        sdk.perform(SavePinCommand(party.id, pin))
    } exercise {
        sdk.getPins(party.id)
    } verify { result ->
        result.size.assertIsEqualTo(1)
        result.first().apply {
            timestamp.isWithinOneSecondOfNow()
            modifyingUserId.assertIsNotEqualTo(null, "As long as an id exists, we're good.")
        }
    }
}
