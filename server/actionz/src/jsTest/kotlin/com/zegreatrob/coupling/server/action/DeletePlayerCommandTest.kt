package com.zegreatrob.coupling.server.action

import com.zegreatrob.coupling.action.VoidResult
import com.zegreatrob.coupling.action.player.DeletePlayerCommand
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.player.PlayerId
import com.zegreatrob.coupling.repository.player.PlayerDelete
import com.zegreatrob.coupling.server.action.player.ServerDeletePlayerCommandDispatcher
import com.zegreatrob.coupling.stubmodel.stubPartyId
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.minspy.Spy
import com.zegreatrob.minspy.SpyData
import com.zegreatrob.minspy.spyFunction
import com.zegreatrob.testmints.async.asyncSetup
import kotools.types.text.toNotBlankString
import kotlin.test.Test

class DeletePlayerCommandTest {

    @Test
    fun willUseRepositoryToRemove() = asyncSetup(object : ServerDeletePlayerCommandDispatcher {
        val playerId = PlayerId("ThatGuyGetHim".toNotBlankString().getOrThrow())
        override val playerRepository = PlayerRepositorySpy().apply { whenever(playerId, true) }
        override val currentPartyId = stubPartyId()
    }) exercise {
        perform(DeletePlayerCommand(currentPartyId, playerId))
    } verify { result ->
        result.assertIsEqualTo(VoidResult.Accepted)
        playerRepository.spyReceivedValues.assertIsEqualTo(listOf(playerId))
    }

    class PlayerRepositorySpy :
        PlayerDelete,
        Spy<PlayerId, Boolean> by SpyData() {
        override suspend fun deletePlayer(partyId: PartyId, playerId: PlayerId): Boolean = spyFunction(playerId)
    }
}
