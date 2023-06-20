package com.zegreatrob.coupling.sdk

import com.benasher44.uuid.uuid4
import com.zegreatrob.coupling.action.party.DeletePartyCommand
import com.zegreatrob.coupling.action.party.SavePartyCommand
import com.zegreatrob.coupling.action.pin.DeletePinCommand
import com.zegreatrob.coupling.action.pin.SavePinCommand
import com.zegreatrob.coupling.model.PartyRecord
import com.zegreatrob.coupling.model.party.PartyElement
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.pin.pin
import com.zegreatrob.coupling.repository.validation.verifyWithWait
import com.zegreatrob.coupling.sdk.gql.graphQuery
import com.zegreatrob.coupling.stubmodel.stubPartyDetails
import com.zegreatrob.coupling.stubmodel.stubPin
import com.zegreatrob.minassert.assertContains
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.minassert.assertIsNotEqualTo
import kotlin.test.Test

class SdkPinTest {

    private val partySetup = asyncSetup.extend(
        sharedSetup = { _ ->
            val sdk = sdk()
            object : CouplingSdk by sdk {
                val party = stubPartyDetails()
            }.apply { sdk.perform(SavePartyCommand(party)) }
        },
        sharedTeardown = { it.perform(DeletePartyCommand(it.party.id)) },
    )

    @Test
    fun canSaveAndGetPins() = partySetup.with(
        {
            object : CouplingSdk by it {
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
        perform(graphQuery { party(party.id) { pinList() } })
            ?.party
            ?.pinList
            ?.map(PartyRecord<Pin>::data)
            ?.map(PartyElement<Pin>::pin)
            .assertIsEqualTo(pins)
    }

    @Test
    fun whenPinDoesNotExistDeleteWillDoNothing() = partySetup() exercise {
        runCatching { perform(DeletePinCommand(party.id, "${uuid4()}")) }
    } verify { result ->
        result.exceptionOrNull()
            .assertIsEqualTo(null)
    }

    @Test
    fun givenNoPinsWillReturnEmptyList() = partySetup() exercise {
        perform(graphQuery { party(party.id) { pinList() } })
            ?.party
            ?.pinList
    } verify { result ->
        result.assertIsEqualTo(emptyList())
    }

    @Test
    fun saveThenDeleteWillNotShowThatPin() = partySetup.with({
        object : CouplingSdk by it {
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
        perform(graphQuery { party(party.id) { pinList() } })
            ?.party
            ?.pinList
            .let { it ?: emptyList() }
            .map { it.data.pin }
            .assertContains(this.pins[0])
            .assertContains(this.pins[2])
            .size
            .assertIsEqualTo(2)
    }

    @Test
    fun saveWorksWithNullableValuesAndAssignsIds() = partySetup.with({
        object : CouplingSdk by it {
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
        perform(graphQuery { party(partyId) { pinList() } })
            ?.party
            ?.pinList
            .let { it ?: emptyList() }
            .map { it.data.pin }
            .also { it.assertHasIds() }
            .map { it.copy(id = null) }
            .assertIsEqualTo(listOf(this.pin))
    }

    private fun List<Pin>.assertHasIds() {
        forEach { pin -> pin.id.assertIsNotEqualTo(null) }
    }

    @Test
    fun givenNoAuthGetIsNotAllowed() = asyncSetup(object {
        val otherParty = stubPartyDetails()
        suspend fun otherSdk() = altAuthorizedSdkDeferred.await()
    }) {
        otherSdk().perform(SavePartyCommand(otherParty))
        otherSdk().perform(SavePinCommand(otherParty.id, stubPin()))
    } exercise {
        sdk().perform(graphQuery { party(otherParty.id) { pinList() } })
            ?.party
            ?.pinList
    } verifyAnd { result ->
        result.assertIsEqualTo(null)
    } teardown {
        otherSdk().perform(DeletePartyCommand(otherParty.id))
    }

    @Test
    fun savedPinsIncludeModificationDateAndUsername() = asyncSetup(object {
        val party = stubPartyDetails()
        val pin = stubPin()
        lateinit var sdk: CouplingSdk
    }) {
        sdk = sdk()
        sdk.perform(SavePartyCommand(party))
        sdk.perform(SavePinCommand(party.id, pin))
    } exercise {
        sdk.perform(graphQuery { party(party.id) { pinList() } })
            ?.party
            ?.pinList
            ?: emptyList()
    } verify { result ->
        result.size.assertIsEqualTo(1)
        result.first().apply {
            timestamp.isWithinOneSecondOfNow()
            modifyingUserId.assertIsNotEqualTo(null, "As long as an id exists, we're good.")
        }
    }
}
