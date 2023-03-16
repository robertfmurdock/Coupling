package com.zegreatrob.coupling.server.action.player

import com.benasher44.uuid.uuid4
import com.soywiz.klock.DateTime
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.party.PartyElement
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.party.with
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.player.player
import com.zegreatrob.coupling.repository.player.PlayerListGet
import com.zegreatrob.coupling.testaction.verifySuccess
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.minspy.Spy
import com.zegreatrob.minspy.SpyData
import com.zegreatrob.minspy.spyFunction
import com.zegreatrob.testmints.async.asyncSetup
import kotlin.test.Test

class PlayersQueryTest {

    @Test
    fun willReturnPlayersFromRepository() = asyncSetup(object : PlayersQueryDispatcher {
        override val currentPartyId = PartyId("Excellent Party")
        val players = listOf(
            Player(
                id = "1",
                callSignAdjective = "Red",
                callSignNoun = "Horner",
            ),
            Player(id = "2", callSignAdjective = "Blue", callSignNoun = "Bee"),
            Player(
                id = "3",
                callSignAdjective = "Green",
                callSignNoun = "Tacos",
            ),
        )

        override val playerRepository = PlayerRepositorySpy()
            .apply { whenever(currentPartyId, players.map { toRecord(it, currentPartyId) }) }
    }) exercise {
        perform(PlayersQuery)
    } verifySuccess { result ->
        result.map { it.data.player }.assertIsEqualTo(players)
    }

    private fun toRecord(it: Player, authorizedPartyId: PartyId) =
        Record(
            authorizedPartyId.with(it),
            "${uuid4()}@email.com",
            false,
            DateTime.now(),
        )

    @Test
    fun willReturnPlayersFromRepositoryAndAutoAssignThemCallSigns() = asyncSetup(object : PlayersQueryDispatcher {
        override val currentPartyId = PartyId("Excellent Party")
        val players = listOf(
            Player(id = "1"),
            Player(id = "2"),
            Player(id = "3"),
        )
        override val playerRepository = PlayerRepositorySpy()
            .apply { whenever(currentPartyId, players.map { toRecord(it, currentPartyId) }) }
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

    class PlayerRepositorySpy : PlayerListGet, Spy<PartyId, List<Record<PartyElement<Player>>>> by SpyData() {
        override suspend fun getPlayers(partyId: PartyId) = spyFunction(partyId)
    }
}
