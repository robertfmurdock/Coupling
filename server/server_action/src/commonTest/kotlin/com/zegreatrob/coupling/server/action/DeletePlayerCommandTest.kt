package com.zegreatrob.coupling.server.action

import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.repository.player.PlayerDelete
import com.zegreatrob.coupling.server.action.player.DeletePlayerCommand
import com.zegreatrob.coupling.server.action.player.DeletePlayerCommandDispatcher
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.minspy.Spy
import com.zegreatrob.minspy.SpyData
import com.zegreatrob.testmints.async.asyncSetup
import kotlin.test.Test

class DeletePlayerCommandTest {
    @Test
    fun willUseRepositoryToRemove() = asyncSetup(object : DeletePlayerCommandDispatcher {
        val playerId = "ThatGuyGetHim"
        override val playerRepository = PlayerRepositorySpy().apply { whenever(playerId, true) }
    }) exercise {
        DeletePlayerCommand(TribeId(""), playerId)
            .perform()
    } verify { result ->
        result.assertIsEqualTo(true)
        playerRepository.spyReceivedValues.assertIsEqualTo(listOf(playerId))
    }

    class PlayerRepositorySpy : PlayerDelete, Spy<String, Boolean> by SpyData() {
        override suspend fun deletePlayer(tribeId: TribeId, playerId: String): Boolean = spyFunction(playerId)
    }
}