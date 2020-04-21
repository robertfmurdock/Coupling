package com.zegreatrob.coupling.mongo.player

import Spy
import SpyData
import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuid4
import com.soywiz.klock.DateTime
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.player.TribeIdPlayer
import com.zegreatrob.coupling.model.player.player
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.model.tribe.with
import com.zegreatrob.coupling.repository.player.PlayerListGet
import com.zegreatrob.coupling.server.action.player.PlayersQuery
import com.zegreatrob.coupling.server.action.player.PlayersQueryDispatcher
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.setupAsync
import com.zegreatrob.testmints.async.testAsync
import kotlin.test.Test

class PlayersQueryTest {

    @Test
    fun willReturnPlayersFromRepository() = testAsync {
        setupAsync(object : PlayersQueryDispatcher {
            override val traceId = uuid4()
            override val currentTribeId = TribeId("Excellent Tribe")
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
                .apply { whenever(currentTribeId, players.map { toRecord(it, currentTribeId) }) }
        }) exerciseAsync {
            PlayersQuery.perform()
        } verifyAsync { result ->
            result.map { it.data.player }.assertIsEqualTo(players)
        }
    }

    private fun toRecord(it: Player, authorizedTribeId: TribeId) =
        Record(
            authorizedTribeId.with(it),
            "${uuid4()}@email.com",
            false,
            DateTime.now()
        )

    @Test
    fun willReturnPlayersFromRepositoryAndAutoAssignThemCallSigns() = testAsync {
        setupAsync(object : PlayersQueryDispatcher {
            override val traceId = uuid4()
            override val currentTribeId = TribeId("Excellent Tribe")
            val players = listOf(
                Player(id = "1"),
                Player(id = "2"),
                Player(id = "3")
            )
            override val playerRepository = PlayerRepositorySpy()
                .apply { whenever(currentTribeId, players.map { toRecord(it, currentTribeId) }) }
        }) exerciseAsync {
            PlayersQuery.perform()
        } verifyAsync { result ->
            result.map { it.data.player }
                .apply {
                    map(Player::id)
                        .assertIsEqualTo(players.map(Player::id))
                    mapNotNull(Player::callSignAdjective).size
                        .assertIsEqualTo(players.size)
                    mapNotNull(Player::callSignNoun).size
                        .assertIsEqualTo(players.size)
                }
        }
    }


    class PlayerRepositorySpy : PlayerListGet, Spy<TribeId, List<Record<TribeIdPlayer>>> by SpyData() {
        override suspend fun getPlayers(tribeId: TribeId) = spyFunction(tribeId)
    }
}
