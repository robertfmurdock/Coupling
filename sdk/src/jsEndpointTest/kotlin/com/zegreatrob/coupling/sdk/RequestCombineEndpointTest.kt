package com.zegreatrob.coupling.sdk

import com.benasher44.uuid.uuid4
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.pin.TribeIdPin
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.player.TribeIdPlayer
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.setupAsync
import com.zegreatrob.testmints.async.testAsync
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlin.test.Test

class RequestCombineEndpointTest {

    @Test
    fun postPlayersAndPinsThenGet() = testAsync {
        val sdk = authorizedSdk()
        setupAsync(object {
            val tribe = Tribe(id = TribeId("et-${uuid4()}"))
            val playersToSave = listOf(
                Player(
                    id = "${uuid4()}",
                    name = "Awesome-O",
                    callSignAdjective = "Awesome",
                    callSignNoun = "Sauce"
                )
            )
            val pinsToSave = listOf(Pin(uuid4().toString(), "1"))
        }) {
            sdk.save(tribe)
            pinsToSave.forEach { sdk.save(TribeIdPin(tribe.id, it)) }
            playersToSave
                .map { TribeIdPlayer(tribe.id, it) }
                .forEach { sdk.save(it) }
        } exerciseAsync {
            coroutineScope {
                val a1 = async { sdk.getPlayers(tribe.id) }
                val a2 = async { sdk.getPins(tribe.id) }
                a1.await() to a2.await()
            }
        } verifyAsync { (players, pins) ->
            players.assertIsEqualTo(playersToSave)
            pins.assertIsEqualTo(pinsToSave)
        }
    }

}