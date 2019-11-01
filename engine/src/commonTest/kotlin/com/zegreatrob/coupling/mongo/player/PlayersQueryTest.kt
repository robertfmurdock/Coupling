package com.zegreatrob.coupling.mongo.player

import Spy
import SpyData
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.setupAsync
import com.zegreatrob.testmints.async.testAsync
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlin.test.Test

class PlayersQueryTest {

    @Test
    fun willReturnPlayersFromRepository() = testAsync {
        setupAsync(object : PlayersQueryDispatcher {
            val tribeId = TribeId("Excellent Tribe")
            val players = listOf(
                Player(
                    id = "1",
                    callSignAdjective = "Red",
                    callSignNoun = "Horner"
                ),
                Player(id = "2", callSignAdjective = "Blue", callSignNoun = "Bee"),
                Player(
                    id = "3",
                    callSignAdjective = "Green",
                    callSignNoun = "Tacos"
                )
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

    @Test
    fun willReturnPlayersFromRepositoryAndAutoAssignThemCallSigns() = testAsync {
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
            result.map(Player::id)
                    .assertIsEqualTo(players.map(Player::id))
            result.mapNotNull(Player::callSignAdjective).size
                    .assertIsEqualTo(players.size)
            result.mapNotNull(Player::callSignNoun).size
                    .assertIsEqualTo(players.size)
        }
    }



    class PlayerRepositorySpy : PlayerGetter, Spy<TribeId, Deferred<List<Player>>> by SpyData() {
        override fun getPlayersAsync(tribeId: TribeId) = spyFunction(tribeId)
    }
}
