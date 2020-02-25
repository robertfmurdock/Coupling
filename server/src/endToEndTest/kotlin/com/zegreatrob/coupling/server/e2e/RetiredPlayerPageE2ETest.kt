package com.zegreatrob.coupling.server.e2e

import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.model.tribe.with
import com.zegreatrob.coupling.server.e2e.CouplingLogin.sdkProvider
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.setupAsync
import com.zegreatrob.testmints.async.testAsync
import kotlinx.coroutines.await
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.test.Test

class RetiredPlayerPageE2ETest {

    companion object {
        private suspend fun delete(players: List<Player>, sdk: AuthorizedSdk, tribe: Tribe) {
            coroutineScope {
                players.forEach { launch { sdk.deletePlayer(tribe.id, it.id!!) } }
            }
        }
    }

    @Test
    fun showsTheRetiredPlayers() = testAsync {
        val sdk = sdkProvider.await()
        setupAsync(object {
            val tribe = "${randomInt()}-RetiredPlayerPageE2ETest"
                .let { Tribe(it.let(::TribeId), name = "$it-name") }
            val players = (1..4)
                .map { "${randomInt()}-RetiredPlayerPageE2ETest-$it" }
                .map { id -> Player(id, name = "$id-name") }
                .toList()
            val notDeletedPlayer = players[2]
            val retiredPlayers = players - notDeletedPlayer
        }) {
            sdk.save(tribe)
            players.forEach { sdk.save(tribe.id.with(it)) }
            delete(retiredPlayers, sdk, tribe)
            CouplingLogin.login.await()
        } exerciseAsync {
            RetiredPlayersPage.goTo(tribe.id)
        } verifyAsync {
            PlayerCard.playerElements.map { it.getText() }.await().toList()
                .assertIsEqualTo(retiredPlayers.map { it.name })
            TribeCard.element.getText().await()
                .assertIsEqualTo(tribe.name)
        }
    }
}
