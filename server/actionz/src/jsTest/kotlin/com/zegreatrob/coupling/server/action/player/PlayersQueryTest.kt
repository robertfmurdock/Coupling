package com.zegreatrob.coupling.server.action.player

import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.party.PartyElement
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.party.with
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.player.player
import com.zegreatrob.coupling.repository.player.PlayerListGet
import com.zegreatrob.coupling.stubmodel.stubPlayer
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.minspy.Spy
import com.zegreatrob.minspy.SpyData
import com.zegreatrob.minspy.spyFunction
import com.zegreatrob.testmints.async.asyncSetup
import kotools.types.text.toNotBlankString
import kotlin.test.Test
import kotlin.time.Clock
import kotlin.uuid.Uuid

class PlayersQueryTest {

    @Test
    fun willReturnPlayersFromRepository() = asyncSetup(object :
        PlayersQuery.Dispatcher {
        val currentPartyId = PartyId("Excellent Party")
        val players = listOf(
            stubPlayer().copy(callSignAdjective = "Red", callSignNoun = "Horner"),
            stubPlayer().copy(callSignAdjective = "Blue", callSignNoun = "Bee"),
            stubPlayer().copy(callSignAdjective = "Green", callSignNoun = "Tacos"),
        )

        override val playerRepository = PlayerRepositorySpy()
            .apply { whenever(currentPartyId, players.map { toRecord(it, currentPartyId) }) }
    }) exercise {
        perform(PlayersQuery(currentPartyId))
    } verify { result ->
        result.map { it.data.player }.assertIsEqualTo(players)
    }

    private fun toRecord(it: Player, authorizedPartyId: PartyId) = Record(
        authorizedPartyId.with(it),
        "${Uuid.random()}@email.com".toNotBlankString().getOrThrow(),
        false,
        Clock.System.now(),
    )

    @Test
    fun willReturnPlayersFromRepositoryAndAutoAssignThemCallSigns() = asyncSetup(object :
        PlayersQuery.Dispatcher {
        val currentPartyId = PartyId("Excellent Party")
        val players = listOf(
            stubPlayer(),
            stubPlayer(),
            stubPlayer(),
        )
        override val playerRepository = PlayerRepositorySpy()
            .apply { whenever(currentPartyId, players.map { toRecord(it, currentPartyId) }) }
    }) exercise {
        perform(PlayersQuery(currentPartyId))
    } verify { result ->
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

    class PlayerRepositorySpy :
        PlayerListGet,
        Spy<PartyId, List<Record<PartyElement<Player>>>> by SpyData() {
        override suspend fun getPlayers(partyId: PartyId) = spyFunction(partyId)
    }
}
