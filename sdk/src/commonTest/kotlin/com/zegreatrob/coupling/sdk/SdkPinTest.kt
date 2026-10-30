package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.action.party.DeletePartyCommand
import com.zegreatrob.coupling.action.party.SavePartyCommand
import com.zegreatrob.coupling.action.party.fire
import com.zegreatrob.coupling.action.pin.DeletePinCommand
import com.zegreatrob.coupling.action.pin.SavePinCommand
import com.zegreatrob.coupling.action.pin.fire
import com.zegreatrob.coupling.model.pin.PinId
import com.zegreatrob.coupling.repository.validation.verifyWithWait
import com.zegreatrob.coupling.sdk.gql.GqlQuery
import com.zegreatrob.coupling.sdk.mapper.toDomain
import com.zegreatrob.coupling.sdk.schema.PinListQuery
import com.zegreatrob.coupling.sdk.schema.PinRecordListQuery
import com.zegreatrob.coupling.sdk.schema.SavePinsForTwoPartiesMutation
import com.zegreatrob.coupling.sdk.schema.type.SavePinInput
import com.zegreatrob.coupling.stubmodel.stubPartyDetails
import com.zegreatrob.coupling.stubmodel.stubPin
import com.zegreatrob.minassert.assertContains
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.minassert.assertIsNotEqualTo
import com.zegreatrob.testmints.action.ActionCannon
import kotlin.test.Test
import kotlin.time.Clock
import kotlin.time.Instant

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
    fun authorizedMutationsInOneRequestSavePinsUnderTheirRequestedParties() = asyncSetup(object {
        val firstParty = stubPartyDetails()
        val secondParty = stubPartyDetails()
        val firstPin = stubPin()
        val secondPin = stubPin()
    }) {
        sdk().fire(SavePartyCommand(firstParty))
        sdk().fire(SavePartyCommand(secondParty))
    } exercise {
        val response = sdk().executeMutation(
            SavePinsForTwoPartiesMutation(
                SavePinInput(firstParty.id, firstPin.id, firstPin.icon, firstPin.name),
                SavePinInput(secondParty.id, secondPin.id, secondPin.icon, secondPin.name),
            ),
        )
        Triple(
            response,
            sdk().fire(GqlQuery(PinListQuery(firstParty.id)))?.party?.pinList,
            sdk().fire(GqlQuery(PinListQuery(secondParty.id)))?.party?.pinList,
        )
    } verify { (response, firstPins, secondPins) ->
        response.errors.assertIsEqualTo(null)
        response.data?.first.assertIsEqualTo(true)
        response.data?.second.assertIsEqualTo(true)
        firstPins?.map { it.pinDetails.toDomain() }.assertIsEqualTo(listOf(firstPin))
        secondPins?.map { it.pinDetails.toDomain() }.assertIsEqualTo(listOf(secondPin))
    }

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
        sdk.fire(GqlQuery(PinListQuery(party.id)))
            ?.party
            ?.pinList
            ?.map { it.pinDetails.toDomain() }
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
        sdk.fire(GqlQuery(PinListQuery(party.id)))
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
        sdk.fire(GqlQuery(PinListQuery(party.id)))
            ?.party
            ?.pinList
            .let { it ?: emptyList() }
            .map { it.pinDetails.toDomain() }
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
        sdk().fire(GqlQuery(PinListQuery(otherParty.id)))
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
        lateinit var pinSaveStartedAt: Instant
        lateinit var pinSaveCompletedAt: Instant
    }) {
        sdk = sdk()
        sdk.fire(SavePartyCommand(party))
        pinSaveStartedAt = Clock.System.now()
        sdk.fire(SavePinCommand(party.id, pin))
        pinSaveCompletedAt = Clock.System.now()
    } exercise {
        sdk.fire(GqlQuery(PinRecordListQuery(party.id)))
            ?.party
            ?.pinList
            ?: emptyList()
    } verify { result ->
        result.size.assertIsEqualTo(1)
        result.first().apply {
            timestamp.isWithinWindow(pinSaveStartedAt, pinSaveCompletedAt)
            modifyingUserEmail.assertIsNotEqualTo(null, "As long as an id exists, we're good.")
        }
    }
}
