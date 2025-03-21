package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.action.party.SavePartyCommand
import com.zegreatrob.coupling.action.party.fire
import com.zegreatrob.coupling.action.pin.SavePinCommand
import com.zegreatrob.coupling.action.pin.fire
import com.zegreatrob.coupling.action.player.SavePlayerCommand
import com.zegreatrob.coupling.action.player.fire
import com.zegreatrob.coupling.model.elements
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.pin.PinId
import com.zegreatrob.coupling.sdk.gql.graphQuery
import com.zegreatrob.coupling.stubmodel.stubPlayer
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.asyncSetup
import kotlinx.coroutines.coroutineScope
import kotlin.test.Test
import kotlin.uuid.Uuid

class RequestCombineEndpointTest {
    @Test
    fun postPlayersAndPinsThenGet() = asyncSetup.with({
        val sdk = sdk()
        object {
            val sdk = sdk
            val party = PartyDetails(id = PartyId("et-${Uuid.random()}"))
            val playersToSave = listOf(
                stubPlayer().copy(
                    name = "Awesome-O",
                    callSignAdjective = "Awesome",
                    callSignNoun = "Sauce",
                ),
            )
            val pinsToSave = listOf(Pin(PinId.new(), "1"))
        }
    }) {
        sdk.fire(SavePartyCommand(party))
        pinsToSave.forEach { sdk.fire(SavePinCommand(party.id, it)) }
        playersToSave.forEach { sdk.fire(SavePlayerCommand(party.id, it)) }
    } exercise {
        coroutineScope {
            sdk.fire(
                graphQuery {
                    party(party.id) {
                        playerList()
                        pinList()
                    }
                },
            )
                ?.party
                .let { it?.playerList?.elements to it?.pinList?.elements }
        }
    } verify { (players, pins) ->
        players.assertIsEqualTo(playersToSave)
        pins.assertIsEqualTo(pinsToSave)
    }
}
