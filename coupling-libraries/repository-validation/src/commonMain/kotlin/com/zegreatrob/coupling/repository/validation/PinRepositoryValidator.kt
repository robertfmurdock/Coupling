package com.zegreatrob.coupling.repository.validation

import com.benasher44.uuid.uuid4
import com.soywiz.klock.DateTime
import com.soywiz.klock.hours
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.pin.pin
import com.zegreatrob.coupling.model.party.with
import com.zegreatrob.coupling.repository.pin.PinRepository
import com.zegreatrob.coupling.stubmodel.stubPin
import com.zegreatrob.minassert.assertContains
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.minassert.assertIsNotEqualTo
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlin.test.Test
import kotlin.time.ExperimentalTime

@ExperimentalCoroutinesApi
@ExperimentalTime
interface PinRepositoryValidator<R : PinRepository> : RepositoryValidator<R, TribeContext<R>> {

    @Test
    fun canSaveAndGetPins() = repositorySetup.with(object : TribeContextMint<R>() {
        val pins = listOf(
            stubPin(),
            stubPin(),
            stubPin()
        )
    }.bind()) exercise {
        tribeId.with(pins).forEach { repository.save(it) }
    } verifyWithWait {
        repository.getPins(tribeId)
            .map { it.data.pin }
            .assertIsEqualTo(pins)
    }

    @Test
    fun saveWorksWithNullableValuesAndAssignsIds() = repositorySetup.with(object : TribeContextMint<R>() {
        val pin = Pin(
            id = null,
            name = "",
            icon = ""
        )
    }.bind()) exercise {
        repository.save(tribeId.with(pin))
    } verifyWithWait {
        repository.getPins(tribeId).map { it.data.pin }
            .also { it.assertHasIds() }
            .map { it.copy(id = null) }
            .assertIsEqualTo(listOf(pin))
    }

    private fun List<Pin>.assertHasIds() {
        forEach { pin -> pin.id.assertIsNotEqualTo(null) }
    }

    @Test
    fun saveThenDeleteWillNotShowThatPin() = repositorySetup.with(object : TribeContextMint<R>() {
        val pins = listOf(
            stubPin(),
            stubPin(),
            stubPin()
        )
    }.bind()) exercise {
        tribeId.with(pins).forEach {
            repository.save(it)
        }
        repository.deletePin(tribeId, pins[1].id!!)
    } verifyWithWait {
        repository.getPins(tribeId).map { it.data.pin }
            .assertContains(pins[0])
            .assertContains(pins[2])
            .size
            .assertIsEqualTo(2)
    }

    @Test
    fun deleteWillFailWhenPinDoesNotExist() = repositorySetup.with(object : TribeContextMint<R>() {
    }.bind()) {
    } exercise {
        repository.deletePin(tribeId, "${uuid4()}")
    } verify { result ->
        result.assertIsEqualTo(false)
    }

    @Test
    fun givenNoPinsWillReturnEmptyList() = repositorySetup {
    } exercise {
        repository.getPins(tribeId)
    } verify { result ->
        result.assertIsEqualTo(emptyList())
    }

    @Test
    fun savedPinsIncludeModificationDateAndUsername() = repositorySetup.with(object : TribeContextMint<R>() {
        val pin = stubPin()
    }.bind()) exercise {
        clock.currentTime = DateTime.now().plus(4.hours)
        repository.save(tribeId.with(pin))
    } verifyWithWait {
        val result = repository.getPins(tribeId)
        result.size.assertIsEqualTo(1)
        result.first().apply {
            timestamp.assertIsEqualTo(clock.currentTime)
            modifyingUserId.assertIsEqualTo(user.email)
        }
    }

}