package com.zegreatrob.coupling.mongo.player

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
            override val playerRepository = PlayerRepositorySpy().apply { whenever(playerId, true) }
        }) exerciseAsync {
            DeletePlayerCommand(playerId)
                    .perform()
        } verifyAsync { result ->
            result.assertIsEqualTo(true)
            playerRepository.spyReceivedValues.assertIsEqualTo(listOf(playerId))
        }
    }

    class PlayerRepositorySpy : PlayerDeleter, Spy<String, Boolean> by SpyData() {
        override suspend fun deletePlayer(playerId: String): Boolean = spyFunction(playerId)
    }
}