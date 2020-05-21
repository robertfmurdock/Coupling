package com.zegreatrob.coupling.server.action.player

import com.benasher44.uuid.uuid4
import com.soywiz.klock.DateTime
import com.zegreatrob.coupling.actionFunc.MasterDispatcher
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.player.TribeIdPlayer
import com.zegreatrob.coupling.model.player.player
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.model.tribe.with
import com.zegreatrob.coupling.repository.player.PlayerListGet
import com.zegreatrob.coupling.testaction.verifySuccess
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.minspy.Spy
import com.zegreatrob.minspy.SpyData
import com.zegreatrob.testmints.async.asyncSetup
import kotlin.test.Test

class PlayersQueryTest {

    @Test
    fun willReturnPlayersFromRepository() = asyncSetup(object : PlayersQueryDispatcher {
        override val masterDispatcher = MasterDispatcher
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
    }) exercise {
        perform(PlayersQuery)
    } verifySuccess { result ->
        result.map { it.data.player }.assertIsEqualTo(players)
    }

    private fun toRecord(it: Player, authorizedTribeId: TribeId) =
        Record(
            authorizedTribeId.with(it),
            "${uuid4()}@email.com",
            false,
            DateTime.now()
        )

    @Test
    fun willReturnPlayersFromRepositoryAndAutoAssignThemCallSigns() = asyncSetup(object : PlayersQueryDispatcher {
        override val masterDispatcher = MasterDispatcher
        override val currentTribeId = TribeId("Excellent Tribe")
        val players = listOf(
            Player(id = "1"),
            Player(id = "2"),
            Player(id = "3")
        )
        override val playerRepository = PlayerRepositorySpy()
            .apply { whenever(currentTribeId, players.map { toRecord(it, currentTribeId) }) }
    }) exercise {
        perform(PlayersQuery)
    } verifySuccess { result ->
        result.map { it.data.player }
            .apply {
                map(Player::id)
                    .assertIsEqualTo(players.map(Player::id))
                map(Player::callSignAdjective).filterNot(String::isEmpty).size
                    .assertIsEqualTo(players.size)
                map(Player::callSignNoun).filterNot(String::isEmpty).size
                    .assertIsEqualTo(players.size)
            }
    }

    class PlayerRepositorySpy : PlayerListGet, Spy<TribeId, List<Record<TribeIdPlayer>>> by SpyData() {
        override suspend fun getPlayers(tribeId: TribeId) = spyFunction(tribeId)
    }
}

