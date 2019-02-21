package com.zegreatrob.coupling.server.entity.player

import Spy
import SpyData
import com.zegreatrob.coupling.common.entity.player.Player
import com.zegreatrob.coupling.common.entity.tribe.TribeId
import com.zegreatrob.minassert.assertIsEqualTo
import exerciseAsync
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import setupAsync
import testAsync
import verifyAsync
import kotlin.test.Test

class PlayersQueryTest {

    @Test
    fun willReturnPlayersFromRepository() = testAsync {
        setupAsync(object : PlayersQueryDispatcher {
            val tribeId = TribeId("Excellent Tribe")
            val players = listOf(
                    Player(id = "1"),
                    Player(id = "2"),
                    Player(id = "3")
            )
            override val playerRepository = PlayerRepositorySpy()
                    .apply { whenever(tribeId, CompletableDeferred(players)) }
        }) exerciseAsync {
            PlayersQuery(tribeId)
                    .perform()
        } verifyAsync { result ->
            result.assertIsEqualTo(players)
        }
    }

    class PlayerRepositorySpy : PlayerGetter, Spy<TribeId, Deferred<List<Player>>> by SpyData() {
        override fun getPlayersAsync(tribeId: TribeId) = spyFunction(tribeId)
    }
}
