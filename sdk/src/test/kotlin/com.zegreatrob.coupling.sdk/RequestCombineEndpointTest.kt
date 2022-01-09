package com.zegreatrob.coupling.sdk

import com.benasher44.uuid.uuid4
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.pin.pin
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.player.player
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.model.tribe.with
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.asyncSetup
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlin.test.Test

class RequestCombineEndpointTest {

    @Test
    fun postPlayersAndPinsThenGet() = asyncSetup({
        val sdk = authorizedKtorSdk()
        object {
            val sdk = sdk
            val tribe = Tribe(id = TribeId("et-${uuid4()}"))
            val playersToSave = listOf(
                Player(id = "${uuid4()}", name = "Awesome-O", callSignAdjective = "Awesome", callSignNoun = "Sauce")
            )
            val pinsToSave = listOf(Pin(uuid4().toString(), "1"))
        }
    }) {
        sdk.save(tribe)
        tribe.id.with(pinsToSave)
            .forEach { sdk.save(it) }
        tribe.id.with(playersToSave)
            .forEach { sdk.save(it) }
    } exercise {
        coroutineScope {
            val a1 = async { sdk.getPlayers(tribe.id).map { it.data.player } }
            val a2 = async { sdk.getPins(tribe.id).map { it.data.pin } }
            a1.await() to a2.await()
        }
    } verify { (players, pins) ->
        players.assertIsEqualTo(playersToSave)
        pins.assertIsEqualTo(pinsToSave)
    }

}