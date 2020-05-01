package com.zegreatrob.coupling.repository.validation

import com.benasher44.uuid.uuid4
import com.soywiz.klock.DateTime
import com.soywiz.klock.TimeProvider
import com.soywiz.klock.hours
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.pin.pin
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.model.tribe.with
import com.zegreatrob.coupling.model.user.User
import com.zegreatrob.coupling.repository.pin.PinRepository
import com.zegreatrob.coupling.stubmodel.stubPin
import com.zegreatrob.minassert.assertContains
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.minassert.assertIsNotEqualTo
import com.zegreatrob.testmints.async.asyncSetup
import com.zegreatrob.testmints.async.testAsync
import com.zegreatrob.testmints.async.waitForTest
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.test.Test

interface PinRepositoryValidator {

    suspend fun withRepository(clock: TimeProvider, handler: suspend (PinRepository, TribeId, User) -> Unit)

    fun testRepository(block: (PinRepository, TribeId, User, MagicClock) -> dynamic) = testAsync {
        val clock = MagicClock()
        withRepository(clock) { repository, tribeId, user ->
            waitForTest { block(repository, tribeId, user, clock) }
        }
    }

    @Test
    fun canSaveAndGetPins() = testRepository { repository, tribeId, _, _ ->
        asyncSetup(object {
            val pins = listOf(
                stubPin(),
                stubPin(),
                stubPin()
            )
        }) exercise {
            tribeId.with(pins).forEach { repository.save(it) }
            repository.getPins(tribeId)
        } verify { result ->
            result.map { it.data.pin }
                .assertIsEqualTo(pins)
        }
    }

    @Test
    fun saveWorksWithNullableValuesAndAssignsIds() = testRepository { repository, tribeId, _, _ ->
        asyncSetup(object {
            val pin = Pin(
                _id = null,
                name = "",
                icon = ""
            )
        }) {
            repository.save(tribeId.with(pin))
        } exercise {
            repository.getPins(tribeId)
        } verify { result ->
            result.map { it.data.pin }
                .also { it.assertHasIds() }
                .map { it.copy(_id = null) }
                .assertIsEqualTo(listOf(pin))
        }
    }

    private fun List<Pin>.assertHasIds() {
        forEach { pin -> pin._id.assertIsNotEqualTo(null) }
    }

    @Test
    fun saveThenDeleteWillNotShowThatPin() = testRepository { repository, tribeId, _, _ ->
        asyncSetup(object {
            val pins = listOf(
                stubPin(),
                stubPin(),
                stubPin()
            )
        }) {
            coroutineScope {
                tribeId.with(pins).forEach {
                    launch { repository.save(it) }
                }
            }
        } exercise {
            repository.deletePin(tribeId, pins[1]._id!!)
            repository.getPins(tribeId)
        } verify { result ->
            result.map { it.data.pin }
                .assertContains(pins[0])
                .assertContains(pins[2])
                .size
                .assertIsEqualTo(2)
        }
    }

    @Test
    fun deleteWillFailWhenPinDoesNotExist() = testRepository { repository, tribeId, _, _ ->
        asyncSetup(object {
        }) {
        } exercise {
            repository.deletePin(tribeId, "${uuid4()}")
        } verify { result ->
            result.assertIsEqualTo(false)
        }
    }

    @Test
    fun givenNoPinsWillReturnEmptyList() = testRepository { repository, tribeId, _, _ ->
        asyncSetup(object {
        }) exercise {
            repository.getPins(tribeId)
        } verify { result ->
            result.assertIsEqualTo(emptyList())
        }
    }

    @Test
    fun savedPinsIncludeModificationDateAndUsername() = testRepository { repository, tribeId, user, clock ->
        asyncSetup(object {
            val pin = stubPin()
        }) {
            clock.currentTime = DateTime.now().plus(4.hours)
            repository.save(tribeId.with(pin))
        } exercise {
            repository.getPins(tribeId)
        } verify { result ->
            result.size.assertIsEqualTo(1)
            result.first().apply {
                timestamp.assertIsEqualTo(clock.currentTime)
                modifyingUserId.assertIsEqualTo(user.email)
            }
        }
    }

}