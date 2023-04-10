package com.zegreatrob.coupling.client.components

import com.benasher44.uuid.uuid4
import com.zegreatrob.coupling.client.components.external.reactwebsocket.reactWebsocket
import com.zegreatrob.coupling.json.toJsonString
import com.zegreatrob.coupling.json.toSerializable
import com.zegreatrob.coupling.model.CouplingSocketMessage
import com.zegreatrob.coupling.model.Message
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.minenzyme.enzyme
import com.zegreatrob.minreact.create
import com.zegreatrob.testmints.setup
import kotlinx.browser.window
import react.create
import react.dom.html.ReactHTML.div
import kotlin.test.Test

class CouplingWebsocketTest {

    @Test
    fun connectsToTheWebsocketUsingParty(): Unit = setup(object {
        val partyId = PartyId("bwahahahaha")
        val useSsl = false
        val token = "${uuid4()}"
    }) { com.zegreatrob.minenzyme.setup } exercise {
        enzyme.shallow(CouplingWebsocket(partyId, useSsl, { }, { div.create {} }, token).create())
    } verify { wrapper ->
        wrapper.find(reactWebsocket).props()
            .url
            .assertIsEqualTo(
                "ws://${window.location.host}/?partyId=${partyId.value}&token=$token",
            )
    }

    @Test
    fun whenSslIsOnWillUseHttps() = setup(object {
        val partyId = PartyId("LOL")
        val useSsl = true
        val token = "${uuid4()}"
    }) { com.zegreatrob.minenzyme.setup } exercise {
        enzyme.shallow(CouplingWebsocket(partyId, useSsl, { }, { div.create {} }, token).create())
    } verify { wrapper ->
        wrapper.find(reactWebsocket).props()
            .url
            .assertIsEqualTo(
                "wss://${window.location.host}/?partyId=LOL&token=$token",
            )
    }

    @Test
    fun whenSocketIsClosedUsesNotConnectedMessage(): Unit = setup(object {
        val partyId = PartyId("Woo")
        var lastMessage: Message? = null
        val wrapper = enzyme.shallow(
            CouplingWebsocket(partyId, false, { lastMessage = it }, { div.create {} }, "")
                .create(),
        )
        val websocketProps = wrapper.find(reactWebsocket).props()
        val expectedMessage = "Not connected"
    }) { com.zegreatrob.minenzyme.setup } exercise {
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
