package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.client.external.reactwebsocket.reactWebsocket
import com.zegreatrob.coupling.json.toJsonString
import com.zegreatrob.coupling.json.toSerializable
import com.zegreatrob.coupling.model.CouplingSocketMessage
import com.zegreatrob.coupling.model.Message
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.minenzyme.shallow
import com.zegreatrob.testmints.setup
import kotlinx.browser.window
import react.ChildrenBuilder
import react.dom.html.ReactHTML.div
import kotlin.test.Test

class CouplingWebsocketTest {

    @Test
    fun connectsToTheWebsocketUsingTribe(): Unit = setup(object {
        val tribeId = TribeId("bwahahahaha")
        val useSsl = false
    }) exercise {
        shallow { child(CouplingWebsocket(tribeId, useSsl, { it: Message -> }, fun ChildrenBuilder.(it: ((Message) -> Unit)?) {
 div {}
})) }
    } verify { wrapper ->
        wrapper.find(reactWebsocket).props()
            .url
            .assertIsEqualTo(
                "ws://${window.location.host}/?tribeId=${tribeId.value}"
            )
    }

    @Test
    fun whenSslIsOnWillUseHttps() = setup(object {
        val tribeId = TribeId("LOL")
        val useSsl = true
    }) exercise {
        shallow { child(CouplingWebsocket(tribeId, useSsl, { it: Message -> }, fun ChildrenBuilder.(it: ((Message) -> Unit)?) {
 div {}
})) }
    } verify { wrapper ->
        wrapper.find(reactWebsocket).props()
            .url
            .assertIsEqualTo(
                "wss://${window.location.host}/?tribeId=LOL"
            )
    }

    @Test
    fun whenSocketIsClosedUsesNotConnectedMessage(): Unit = setup(object {
        val tribeId = TribeId("Woo")
        var lastMessage: Message? = null
        val wrapper = shallow { child(CouplingWebsocket(tribeId, false, { it: Message -> lastMessage = it }, fun ChildrenBuilder.(_: ((Message) -> Unit)?) {
 div {}
})) }
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
