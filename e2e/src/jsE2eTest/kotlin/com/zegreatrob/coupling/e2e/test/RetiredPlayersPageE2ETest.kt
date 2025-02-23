package com.zegreatrob.coupling.e2e.test

import com.zegreatrob.coupling.action.party.SavePartyCommand
import com.zegreatrob.coupling.action.party.fire
import com.zegreatrob.coupling.action.player.DeletePlayerCommand
import com.zegreatrob.coupling.action.player.SavePlayerCommand
import com.zegreatrob.coupling.action.player.fire
import com.zegreatrob.coupling.e2e.test.PartyCard.element
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.player.defaultPlayer
import com.zegreatrob.coupling.sdk.CouplingSdkDispatcher
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.action.ActionCannon
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.test.Test

class RetiredPlayersPageE2ETest {

    companion object {
        private suspend fun delete(
            players: List<Player>,
            sdk: ActionCannon<CouplingSdkDispatcher>,
            party: PartyDetails,
        ) {
            coroutineScope {
                players.forEach { launch { sdk.fire(DeletePlayerCommand(party.id, it.id)) } }
            }
        }
    }

    @Test
    fun showsTheRetiredPlayers() = sdkSetup(object : SdkContext() {
        val party = "${randomInt()}-RetiredPlayerPageE2ETest"
            .let { PartyDetails(it.let(::PartyId), name = "$it-name") }
        val players = (1..4)
            .map { "${randomInt()}-RetiredPlayerPageE2ETest-$it" }
            .map { id -> defaultPlayer.copy(id, name = "$id-name", email = id) }
            .toList()
        val notDeletedPlayer = players[2]
        val retiredPlayers = players - notDeletedPlayer
    }) {
        sdk.fire(SavePartyCommand(party))
        players.forEach { sdk.fire(SavePlayerCommand(party.id, it)) }
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
