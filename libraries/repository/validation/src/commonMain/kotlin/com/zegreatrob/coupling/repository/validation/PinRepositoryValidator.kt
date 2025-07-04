package com.zegreatrob.coupling.repository.validation

import com.zegreatrob.coupling.model.party.with
import com.zegreatrob.coupling.model.pin.PinId
import com.zegreatrob.coupling.model.pin.pin
import com.zegreatrob.coupling.repository.pin.PinRepository
import com.zegreatrob.coupling.stubmodel.stubPin
import com.zegreatrob.minassert.assertContains
import com.zegreatrob.minassert.assertIsEqualTo
import kotlin.test.Test
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours

interface PinRepositoryValidator<R : PinRepository> : RepositoryValidator<R, PartyContext<R>> {

    @Test
    fun canSaveAndGetPins() = repositorySetup.with(
        object : PartyContextMint<R>() {
            val pins = listOf(
                stubPin(),
                stubPin(),
                stubPin(),
            )
        }.bind(),
    ) exercise {
        partyId.with(pins).forEach { repository.save(it) }
    } verifyWithWait {
        repository.getPins(partyId)
            .map { it.data.pin }
            .assertIsEqualTo(pins)
    }

    @Test
    fun saveThenDeleteWillNotShowThatPin() = repositorySetup.with(
        object : PartyContextMint<R>() {
            val pins = listOf(
                stubPin(),
                stubPin(),
                stubPin(),
            )
        }.bind(),
    ) exercise {
        partyId.with(pins).forEach {
            repository.save(it)
        }
        repository.deletePin(partyId, pins[1].id)
    } verifyWithWait {
        repository.getPins(partyId).map { it.data.pin }
            .assertContains(pins[0])
            .assertContains(pins[2])
            .size
            .assertIsEqualTo(2)
    }

    @Test
    fun deleteWillFailWhenPinDoesNotExist() = repositorySetup.with(
        object : PartyContextMint<R>() {
        }.bind(),
    ) exercise {
        repository.deletePin(partyId, PinId.new())
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
    fun savedPinsIncludeModificationDateAndUsername() = repositorySetup.with(
        object : PartyContextMint<R>() {
            val pin = stubPin()
        }.bind(),
    ) exercise {
        clock.currentTime = Clock.System.now().plus(4.hours)
        repository.save(partyId.with(pin))
    } verifyWithWait {
        val result = repository.getPins(partyId)
        result.size.assertIsEqualTo(1)
        result.first().apply {
            timestamp.assertIsEqualTo(clock.currentTime)
            modifyingUserId.assertIsEqualTo(user.id.value)
        }
    }
}
