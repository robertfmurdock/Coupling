package com.zegreatrob.coupling.repositoryvalidation

import com.benasher44.uuid.uuid4
import com.zegreatrob.coupling.model.pin.TribeIdPin
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.repository.pin.PinRepository
import com.zegreatrob.minassert.assertContains
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.setupAsync
import com.zegreatrob.testmints.async.testAsync
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import stubPin
import kotlin.test.Test

interface PinRepositoryValidator {

    suspend fun withRepository(handler: suspend (PinRepository, TribeId) -> Unit)

    private fun testRepository(block: suspend CoroutineScope.(PinRepository, TribeId) -> Any?) = testAsync {
        withRepository { repository, tribeId -> block(repository, tribeId) }
    }

    @Test
    fun canSaveAndGetPins() = testRepository { repository, tribeId ->
        setupAsync(object {
            val pins = listOf(
                stubPin(),
                stubPin(),
                stubPin()
            )
        }) exerciseAsync {
            pins.forEach { repository.save(TribeIdPin(tribeId, it)) }
            repository.getPins(tribeId)
        } verifyAsync { result ->
            result.assertIsEqualTo(pins)
        }
    }

    @Test
    fun saveThenDeleteWillNotShowThatPin() = testRepository { repository, tribeId ->
        setupAsync(object {
            val pins = listOf(
                stubPin(),
                stubPin(),
                stubPin()
            )
        }) {
            coroutineScope {
                pins.forEach { launch { repository.save(TribeIdPin(tribeId, it)) } }
            }
        } exerciseAsync {
            repository.deletePin(tribeId, pins[1]._id!!)
            repository.getPins(tribeId)
        } verifyAsync { result ->
            result.assertContains(pins[0])
                .assertContains(pins[2])
                .size
                .assertIsEqualTo(2)
        }
    }

    @Test
    fun deleteWillFailWhenPinDoesNotExist() = testRepository { repository, tribeId ->
        setupAsync(object {
        }) {
        } exerciseAsync {
            repository.deletePin(tribeId, "${uuid4()}")
        } verifyAsync { result ->
            result.assertIsEqualTo(false)
        }
    }

    @Test
    fun givenNoPinsWillReturnEmptyList() = testRepository { repository, tribeId ->
        setupAsync(object {
        }) exerciseAsync {
            repository.getPins(tribeId)
        } verifyAsync { result ->
            result.assertIsEqualTo(emptyList())
        }
    }

}