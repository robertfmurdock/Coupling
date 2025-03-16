package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.action.party.DeletePartyCommand
import com.zegreatrob.coupling.action.party.SavePartyCommand
import com.zegreatrob.coupling.action.party.fire
import com.zegreatrob.coupling.action.pin.DeletePinCommand
import com.zegreatrob.coupling.action.pin.SavePinCommand
import com.zegreatrob.coupling.action.pin.fire
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
import com.zegreatrob.testmints.action.ActionCannon
import kotools.types.text.toNotBlankString
import kotlin.test.Test
import kotlin.uuid.Uuid

class SdkPinTest {

    private val partySetup = asyncSetup.extend(
        sharedSetup = { _ ->
            val sdk = sdk()
            object {
                val sdk = sdk
                val party = stubPartyDetails()
            }.apply { sdk.fire(SavePartyCommand(party)) }
        },
        sharedTeardown = { it.sdk.fire(DeletePartyCommand(it.party.id)) },
    )

    @Test
    fun canSaveAndGetPins() = partySetup.with(
        {
            object {
                val sdk = it.sdk
                val party = it.party
                val pins = listOf(
                    stubPin(),
                    stubPin(),
                    stubPin(),
                )
            }
        },
    ) exercise {
        pins.forEach { sdk.fire(SavePinCommand(party.id, it)) }
    } verifyWithWait {
        sdk.fire(graphQuery { party(party.id) { pinList() } })
            ?.party
            ?.pinList
            ?.map(PartyRecord<Pin>::data)
            ?.map(PartyElement<Pin>::pin)
            .assertIsEqualTo(pins)
    }

    @Test
    fun whenPinDoesNotExistDeleteWillDoNothing() = partySetup() exercise {
        runCatching { sdk.fire(DeletePinCommand(party.id, "${Uuid.random()}".toNotBlankString().getOrThrow())) }
    } verify { result ->
        result.exceptionOrNull()
            .assertIsEqualTo(null)
    }

    @Test
    fun givenNoPinsWillReturnEmptyList() = partySetup() exercise {
        sdk.fire(graphQuery { party(party.id) { pinList() } })
            ?.party
            ?.pinList
    } verify { result ->
        result.assertIsEqualTo(emptyList())
    }

    @Test
    fun saveThenDeleteWillNotShowThatPin() = partySetup.with({
        object {
            val sdk = it.sdk
            val party = it.party
            val pins = listOf(
                stubPin(),
                stubPin(),
                stubPin(),
            )
        }
    }) exercise {
        pins.forEach { sdk.fire(SavePinCommand(party.id, it)) }
        sdk.fire(DeletePinCommand(party.id, this.pins[1].id))
    } verifyWithWait {
        sdk.fire(graphQuery { party(party.id) { pinList() } })
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
    fun givenNoAuthGetIsNotAllowed() = asyncSetup(object {
        val otherParty = stubPartyDetails()
        suspend fun otherSdk() = altAuthorizedSdkDeferred.await()
    }) {
        otherSdk().fire(SavePartyCommand(otherParty))
        otherSdk().fire(SavePinCommand(otherParty.id, stubPin()))
    } exercise {
        sdk().fire(graphQuery { party(otherParty.id) { pinList() } })
            ?.party
            ?.pinList
    } verifyAnd { result ->
        result.assertIsEqualTo(null)
    } teardown {
        otherSdk().fire(DeletePartyCommand(otherParty.id))
    }

    @Test
    fun savedPinsIncludeModificationDateAndUsername() = asyncSetup(object {
        val party = stubPartyDetails()
        val pin = stubPin()
        lateinit var sdk: ActionCannon<CouplingSdkDispatcher>
    }) {
        sdk = sdk()
        sdk.fire(SavePartyCommand(party))
        sdk.fire(SavePinCommand(party.id, pin))
    } exercise {
        sdk.fire(graphQuery { party(party.id) { pinList() } })
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
