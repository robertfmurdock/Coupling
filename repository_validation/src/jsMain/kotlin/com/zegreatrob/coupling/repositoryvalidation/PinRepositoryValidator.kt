package com.zegreatrob.coupling.repositoryvalidation

import com.zegreatrob.coupling.model.pin.TribeIdPin
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.repository.pin.PinRepository
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.setupAsync
import com.zegreatrob.testmints.async.testAsync
import kotlinx.coroutines.CoroutineScope
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

}