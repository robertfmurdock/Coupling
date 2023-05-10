package com.zegreatrob.coupling.sdk

import com.benasher44.uuid.uuid4
import com.zegreatrob.coupling.action.party.SavePartyCommand
import com.zegreatrob.coupling.action.pin.SavePinCommand
import com.zegreatrob.coupling.action.player.SavePlayerCommand
import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.pin.pin
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.player.player
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.asyncSetup
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlin.test.Test

class RequestCombineEndpointTest {
    @Test
    fun postPlayersAndPinsThenGet() = asyncSetup.with({
        val sdk = sdk()
        object : BarebonesSdk by sdk {
            val party = Party(id = PartyId("et-${uuid4()}"))
            val playersToSave = listOf(
                Player(
                    id = "${uuid4()}",
                    name = "Awesome-O",
                    callSignAdjective = "Awesome",
                    callSignNoun = "Sauce",
                    avatarType = null,
                ),
            )
            val pinsToSave = listOf(Pin(uuid4().toString(), "1"))
        }
    }) {
        perform(SavePartyCommand(party))
        pinsToSave.forEach { perform(SavePinCommand(party.id, it)) }
        playersToSave.forEach { perform(SavePlayerCommand(party.id, it)) }
    } exercise {
        coroutineScope {
            val a1 = async { playerRepository.getPlayers(party.id).map { it.data.player } }
            val a2 = async { pinRepository.getPins(party.id).map { it.data.pin } }
            a1.await() to a2.await()
        }
    } verify { (players, pins) ->
        players.assertIsEqualTo(playersToSave)
        pins.assertIsEqualTo(pinsToSave)
    }
}
