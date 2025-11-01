package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.action.party.DeletePartyCommand
import com.zegreatrob.coupling.action.party.SavePartyCommand
import com.zegreatrob.coupling.action.party.fire
import com.zegreatrob.coupling.action.pin.DeletePinCommand
import com.zegreatrob.coupling.action.pin.SavePinCommand
import com.zegreatrob.coupling.action.pin.fire
import com.zegreatrob.coupling.model.pin.PinId
import com.zegreatrob.coupling.repository.validation.verifyWithWait
import com.zegreatrob.coupling.sdk.gql.ApolloGraphQuery
import com.zegreatrob.coupling.sdk.schema.PinListQuery
import com.zegreatrob.coupling.sdk.schema.PinRecordListQuery
import com.zegreatrob.coupling.stubmodel.stubPartyDetails
import com.zegreatrob.coupling.stubmodel.stubPin
import com.zegreatrob.minassert.assertContains
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.minassert.assertIsNotEqualTo
import com.zegreatrob.testmints.action.ActionCannon
import kotlin.test.Test

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
        sdk.fire(ApolloGraphQuery(PinListQuery(party.id)))
            ?.party
            ?.pinList
            ?.map { it.pinDetailsFragment.toModel() }
            .assertIsEqualTo(pins)
    }

    @Test
    fun whenPinDoesNotExistDeleteWillDoNothing() = partySetup() exercise {
        runCatching { sdk.fire(DeletePinCommand(party.id, PinId.new())) }
    } verify { result ->
        result.exceptionOrNull()
            .assertIsEqualTo(null)
    }

    @Test
    fun givenNoPinsWillReturnEmptyList() = partySetup() exercise {
        sdk.fire(ApolloGraphQuery(PinListQuery(party.id)))
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
        sdk.fire(ApolloGraphQuery(PinListQuery(party.id)))
            ?.party
            ?.pinList
            .let { it ?: emptyList() }
            .map { it.pinDetailsFragment.toModel() }
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
        sdk().fire(ApolloGraphQuery(PinListQuery(otherParty.id)))
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
        sdk.fire(ApolloGraphQuery(PinRecordListQuery(party.id)))
            ?.party
            ?.pinList
            ?: emptyList()
    } verify { result ->
        result.size.assertIsEqualTo(1)
        result.first().apply {
            timestamp.isWithinOneSecondOfNow()
            modifyingUserEmail.assertIsNotEqualTo(null, "As long as an id exists, we're good.")
        }
    }
}
