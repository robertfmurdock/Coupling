package com.zegreatrob.coupling.entity.player

import Spy
import SpyData
import assertIsEqualTo
import exerciseAsync
import setupAsync
import testAsync
import verifyAsync
import kotlin.test.Test

class DeletePlayerCommandTest {
    @Test
    fun willUseRepositoryToRemove() = testAsync {
        setupAsync(object : DeletePlayerCommandDispatcher {
            val playerId = "ThatGuyGetHim"
            override val playerRepository = PlayerRepositorySpy().apply { whenever(playerId, Unit) }
        }) exerciseAsync {
            DeletePlayerCommand(playerId)
                    .perform()
        } verifyAsync { result ->
            result.assertIsEqualTo(playerId)
            playerRepository.spyReceivedValues.assertIsEqualTo(listOf(playerId))
        }
    }

    class PlayerRepositorySpy : PlayerDeleter, Spy<String, Unit> by SpyData() {
        override suspend fun delete(playerId: String) = spyFunction(playerId)
    }
}