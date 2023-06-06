package com.zegreatrob.coupling.server.action

import com.zegreatrob.coupling.action.player.DeletePlayerCommand
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.repository.player.PlayerDelete
import com.zegreatrob.coupling.server.action.player.ServerDeletePlayerCommandDispatcher
import com.zegreatrob.coupling.testaction.verifySuccess
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.minspy.Spy
import com.zegreatrob.minspy.SpyData
import com.zegreatrob.minspy.spyFunction
import com.zegreatrob.testmints.async.asyncSetup
import kotlin.test.Test

class DeletePlayerCommandTest {
    @Test
    fun willUseRepositoryToRemove() = asyncSetup(object : ServerDeletePlayerCommandDispatcher {
        val playerId = "ThatGuyGetHim"
        override val playerRepository = PlayerRepositorySpy().apply { whenever(playerId, true) }
        override val currentPartyId = PartyId("")
    }) exercise {
        perform(DeletePlayerCommand(currentPartyId, playerId))
    } verifySuccess {
        playerRepository.spyReceivedValues.assertIsEqualTo(listOf(playerId))
    }

    class PlayerRepositorySpy : PlayerDelete, Spy<String, Boolean> by SpyData() {
        override suspend fun deletePlayer(partyId: PartyId, playerId: String): Boolean = spyFunction(playerId)
    }
}
