package com.zegreatrob.coupling.client

import com.benasher44.uuid.uuid4
import com.zegreatrob.coupling.client.external.reactwebsocket.reactWebsocket
import com.zegreatrob.coupling.json.toJsonString
import com.zegreatrob.coupling.json.toSerializable
import com.zegreatrob.coupling.model.CouplingSocketMessage
import com.zegreatrob.coupling.model.Message
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.minenzyme.shallow
import com.zegreatrob.testmints.setup
import kotlinx.browser.window
import react.dom.html.ReactHTML.div
import kotlin.test.Test

class CouplingWebsocketTest {

    @Test
    fun connectsToTheWebsocketUsingParty(): Unit = setup(object {
        val partyId = PartyId("bwahahahaha")
        val useSsl = false
        val token = "${uuid4()}"
    }) exercise {
        shallow { child(CouplingWebsocket(partyId, useSsl, { }, token) { div {} }) }
    } verify { wrapper ->
        wrapper.find(reactWebsocket).props()
            .url
            .assertIsEqualTo(
                "ws://${window.location.host}/?tribeId=${partyId.value}&token=$token"
            )
    }

    @Test
    fun whenSslIsOnWillUseHttps() = setup(object {
        val partyId = PartyId("LOL")
        val useSsl = true
        val token = "${uuid4()}"
    }) exercise {
        shallow { child(CouplingWebsocket(partyId, useSsl, { }, token) { div {} }) }
    } verify { wrapper ->
        wrapper.find(reactWebsocket).props()
            .url
            .assertIsEqualTo(
                "wss://${window.location.host}/?tribeId=LOL&token=$token"
            )
    }

    @Test
    fun whenSocketIsClosedUsesNotConnectedMessage(): Unit = setup(object {
        val partyId = PartyId("Woo")
        var lastMessage: Message? = null
        val wrapper = shallow { child(CouplingWebsocket(partyId, false, { lastMessage = it }, "") { div {} }) }
        val websocketProps = wrapper.find(reactWebsocket).props()
        val expectedMessage = "Not connected"
    }) exercise {
        websocketProps.onMessage(socketMessage("lol"))
        wrapper.update()
        websocketProps.onClose()
        wrapper.update()
    } verify {
        lastMessage?.assertIsEqualTo(CouplingSocketMessage(expectedMessage, emptySet()))
    }

    private fun socketMessage(expectedMessage: String) = CouplingSocketMessage(expectedMessage, emptySet(), null)
        .toSerializable()
        .toJsonString()

}
