package com.zegreatrob.coupling.sdk
import com.benasher44.uuid.uuid4
import com.soywiz.klock.DateTime
import com.zegreatrob.coupling.action.party.SavePartyCommand
import com.zegreatrob.coupling.model.party.with
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.pin.pin
import com.zegreatrob.coupling.repository.validation.MagicClock
import com.zegreatrob.coupling.repository.validation.PartyContextMint
import com.zegreatrob.coupling.repository.validation.bind
import com.zegreatrob.coupling.repository.validation.verifyWithWait
import com.zegreatrob.coupling.stubmodel.stubParty
import com.zegreatrob.coupling.stubmodel.stubPin
import com.zegreatrob.minassert.assertContains
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.minassert.assertIsNotEqualTo
import com.zegreatrob.testmints.async.AsyncMints.asyncSetup
import com.zegreatrob.testmints.async.AsyncMints.asyncTestTemplate
import kotlin.test.Test

class SdkPinRepositoryTest {

    private val repositorySetup = asyncTestTemplate<SdkPartyContext<SdkPinRepository>>(sharedSetup = {
        val sdk = authorizedSdk()
        val party = stubParty()
        SdkPartyContext(sdk, sdk.pinRepository, party.id, MagicClock())
            .apply {
                party.save()
            }
    }, sharedTeardown = {
        it.sdk.partyRepository.deleteIt(it.partyId)
    })

    @Test
    fun canSaveAndGetPins() = repositorySetup.with(
        object : PartyContextMint<SdkPinRepository>() {
            val pins = listOf(
                stubPin(),
                stubPin(),
                stubPin(),
            )
        }.bind(),
    ) exercise {
        partyId.with(this.pins).forEach { repository.save(it) }
    } verifyWithWait {
        repository.getPins(partyId)
            .map { it.data.pin }
            .assertIsEqualTo(this.pins)
    }

    @Test
    fun deleteWillFailWhenPinDoesNotExist() = repositorySetup.with(
        object : PartyContextMint<SdkPinRepository>() {
        }.bind(),
    ) {
    } exercise {
        repository.deletePin(partyId, "${uuid4()}")
    } verify { result ->
        result.assertIsEqualTo(false)
    }

    @Test
    fun givenNoPinsWillReturnEmptyList() = repositorySetup {
    } exercise {
        repository.getPins(partyId)
    } verify { result ->
        result.assertIsEqualTo(emptyList())
    }

    @Test
    fun saveThenDeleteWillNotShowThatPin() = repositorySetup.with(
        object : PartyContextMint<SdkPinRepository>() {
            val pins = listOf(
                stubPin(),
                stubPin(),
                stubPin(),
            )
        }.bind(),
    ) exercise {
        partyId.with(this.pins).forEach {
            repository.save(it)
        }
        repository.deletePin(partyId, this.pins[1].id!!)
    } verifyWithWait {
        this.repository.getPins(this.partyId).map { it.data.pin }
            .assertContains(this.pins[0])
            .assertContains(this.pins[2])
            .size
            .assertIsEqualTo(2)
    }

    @Test
    fun saveWorksWithNullableValuesAndAssignsIds() = repositorySetup.with(
        object : PartyContextMint<SdkPinRepository>() {
            val pin = Pin(
                id = null,
                name = "",
                icon = "",
            )
        }.bind(),
    ) exercise {
        repository.save(partyId.with(this.pin))
    } verifyWithWait {
        this.repository.getPins(this.partyId).map { it.data.pin }
            .also { it.assertHasIds() }
            .map { it.copy(id = null) }
            .assertIsEqualTo(listOf(this.pin))
    }

    private fun List<Pin>.assertHasIds() {
        forEach { pin -> pin.id.assertIsNotEqualTo(null) }
    }

    @Test
    fun givenNoAuthGetIsNotAllowed() = asyncSetup.with({
        val sdk = authorizedSdk()
        val otherSdk = altAuthorizedSdkDeferred.await()
        object {
            val otherParty = stubParty()
            val sdk = sdk
            val otherSdk = otherSdk
        }
    }) {
        otherSdk.partyRepository.save(otherParty)
        otherSdk.pinRepository.save(otherParty.id.with(stubPin()))
    } exercise {
        sdk.pinRepository.getPins(otherParty.id)
    } verifyAnd { result ->
        result.assertIsEqualTo(emptyList())
    } teardown {
        otherSdk.partyRepository.deleteIt(otherParty.id)
    }

    @Test
    fun savedPinsIncludeModificationDateAndUsername() = asyncSetup(object {
        val party = stubParty()
        val pin = stubPin()
        lateinit var sdk: SdkSingleton
    }) {
        sdk = authorizedSdk()
        sdk.perform(SavePartyCommand(party))
        sdk.pinRepository.save(party.id.with(pin))
    } exercise {
        sdk.pinRepository.getPins(party.id)
    } verify { result ->
        result.size.assertIsEqualTo(1)
        result.first().apply {
            timestamp.isWithinOneSecondOfNow()
            modifyingUserId.assertIsNotEqualTo(null, "As long as an id exists, we're good.")
        }
    }
}

fun DateTime.isWithinOneSecondOfNow() {
    val timeSpan = DateTime.now() - this
    (timeSpan.seconds < 1)
        .assertIsEqualTo(true)
}
