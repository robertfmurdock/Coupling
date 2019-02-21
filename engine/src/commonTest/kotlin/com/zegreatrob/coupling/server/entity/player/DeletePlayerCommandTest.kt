package com.zegreatrob.coupling.server.entity.player

import Spy
import SpyData
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.setupAsync
import com.zegreatrob.testmints.async.testAsync
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