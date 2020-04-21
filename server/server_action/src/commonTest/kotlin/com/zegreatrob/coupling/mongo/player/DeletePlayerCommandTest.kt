package com.zegreatrob.coupling.mongo.player

import Spy
import SpyData
import com.benasher44.uuid.uuid4
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.repository.player.PlayerDelete
import com.zegreatrob.coupling.server.action.player.DeletePlayerCommand
import com.zegreatrob.coupling.server.action.player.DeletePlayerCommandDispatcher
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.setupAsync
import com.zegreatrob.testmints.async.testAsync
import kotlin.test.Test

class DeletePlayerCommandTest {
    @Test
    fun willUseRepositoryToRemove() = testAsync {
        setupAsync(object : DeletePlayerCommandDispatcher {
            val playerId = "ThatGuyGetHim"
            override val traceId = uuid4()
            override val playerRepository = PlayerRepositorySpy().apply { whenever(playerId, true) }
        }) exerciseAsync {
            DeletePlayerCommand(TribeId(""), playerId)
                .perform()
        } verifyAsync { result ->
            result.assertIsEqualTo(true)
            playerRepository.spyReceivedValues.assertIsEqualTo(listOf(playerId))
        }
    }

    class PlayerRepositorySpy : PlayerDelete, Spy<String, Boolean> by SpyData() {
        override suspend fun deletePlayer(tribeId: TribeId, playerId: String): Boolean = spyFunction(playerId)
    }
}