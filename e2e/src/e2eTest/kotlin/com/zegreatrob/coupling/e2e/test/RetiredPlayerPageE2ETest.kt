package com.zegreatrob.coupling.e2e.test

import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.tribe.Party
import com.zegreatrob.coupling.model.tribe.PartyId
import com.zegreatrob.coupling.model.tribe.with
import com.zegreatrob.coupling.sdk.Sdk
import com.zegreatrob.minassert.assertIsEqualTo
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.test.Test

class RetiredPlayerPageE2ETest {

    companion object {
        private suspend fun delete(players: List<Player>, sdk: Sdk, tribe: Party) {
            coroutineScope {
                players.forEach { launch { sdk.playerRepository.deletePlayer(tribe.id, it.id) } }
            }
        }
    }

    @Test
    fun showsTheRetiredPlayers() = sdkSetup(object : SdkContext() {
        val tribe = "${randomInt()}-RetiredPlayerPageE2ETest"
            .let { Party(it.let(::PartyId), name = "$it-name") }
        val players = (1..4)
            .map { "${randomInt()}-RetiredPlayerPageE2ETest-$it" }
            .map { id -> Player(id, name = "$id-name") }
            .toList()
        val notDeletedPlayer = players[2]
        val retiredPlayers = players - notDeletedPlayer
    }) {
        sdk.tribeRepository.save(tribe)
        players.forEach { sdk.playerRepository.save(tribe.id.with(it)) }
        delete(retiredPlayers, sdk, tribe)
    } exercise {
        RetiredPlayersPage.goTo(tribe.id)
    } verify {
        PlayerCard.playerElements.map { it.text() }.toList()
            .assertIsEqualTo(retiredPlayers.map { it.name })
        TribeCard.element().text()
            .assertIsEqualTo(tribe.name)
    }
}
