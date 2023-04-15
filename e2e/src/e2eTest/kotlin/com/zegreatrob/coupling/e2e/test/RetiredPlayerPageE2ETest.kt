package com.zegreatrob.coupling.e2e.test

import com.zegreatrob.coupling.e2e.test.PartyCard.element
import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.party.with
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.sdk.Sdk
import com.zegreatrob.minassert.assertIsEqualTo
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.test.Test

class RetiredPlayerPageE2ETest {

    companion object {
        private suspend fun delete(players: List<Player>, sdk: Sdk, party: Party) {
            coroutineScope {
                players.forEach { launch { sdk.playerRepository.deletePlayer(party.id, it.id) } }
            }
        }
    }

    @Test
    fun showsTheRetiredPlayers() = sdkSetup(object : SdkContext() {
        val party = "${randomInt()}-RetiredPlayerPageE2ETest"
            .let { Party(it.let(::PartyId), name = "$it-name") }
        val players = (1..4)
            .map { "${randomInt()}-RetiredPlayerPageE2ETest-$it" }
            .map { id -> Player(id, name = "$id-name", avatarType = null) }
            .toList()
        val notDeletedPlayer = players[2]
        val retiredPlayers = players - notDeletedPlayer
    }) {
        sdk.partyRepository.save(party)
        players.forEach { sdk.playerRepository.save(party.id.with(it)) }
        delete(retiredPlayers, sdk, party)
    } exercise {
        RetiredPlayersPage.goTo(party.id)
    } verify {
        PlayerCard.playerElements.map { it.text() }.toList()
            .assertIsEqualTo(retiredPlayers.map { it.name })
        element.text()
            .assertIsEqualTo(party.name)
    }
}
