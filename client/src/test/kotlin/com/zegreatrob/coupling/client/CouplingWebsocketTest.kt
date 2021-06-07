package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.client.external.reactwebsocket.WebsocketProps
import com.zegreatrob.coupling.client.external.reactwebsocket.reactWebsocket
import com.zegreatrob.coupling.model.CouplingSocketMessage
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.minenzyme.shallow
import com.zegreatrob.testmints.invoke
import com.zegreatrob.testmints.setup
import kotlinx.browser.window
import react.dom.div
import kotlin.js.Json
import kotlin.js.json
import kotlin.test.Test

class CouplingWebsocketTest {

    @Test
    fun connectsToTheWebsocketUsingTribe(): Unit = setup(object {
        val tribeId = TribeId("bwahahahaha")
        val useSsl = false
    }) exercise {
        shallow { couplingWebsocket(tribeId, useSsl) { _, _ -> div {} } }
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
        shallow { couplingWebsocket(tribeId, useSsl) { _, _ -> div {} } }
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
        var lastMessage: CouplingSocketMessage? = null
        val wrapper = shallow { couplingWebsocket(tribeId, false) { message, _ -> lastMessage = message; div {} } }
        val websocketProps = wrapper.find(reactWebsocket).props()
            .unsafeCast<WebsocketProps>()
        val expectedMessage = "Not connected"
    }) exercise {
        websocketProps.onMessage(socketMessage("lol"))
        wrapper.update()
        websocketProps.onClose()
        wrapper.update()
    } verify {
        lastMessage?.text.assertIsEqualTo(expectedMessage)
    }

    private fun socketMessage(expectedMessage: String) =
        JSON.stringify(json("type" to "LivePlayers", "text" to expectedMessage, "players" to emptyArray<Json>()))

}
