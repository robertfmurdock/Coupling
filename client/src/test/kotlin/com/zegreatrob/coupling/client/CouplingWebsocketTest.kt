package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.client.external.reactwebsocket.WebsocketProps
import com.zegreatrob.coupling.client.external.reactwebsocket.reactWebsocket
import com.zegreatrob.coupling.json.toSerializable
import com.zegreatrob.coupling.model.CouplingSocketMessage
import com.zegreatrob.coupling.model.Message
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.minenzyme.shallow
import com.zegreatrob.testmints.invoke
import com.zegreatrob.testmints.setup
import kotlinx.browser.window
import kotlinx.serialization.encodeToString
import react.dom.div
import kotlin.test.Test

class CouplingWebsocketTest {

    @Test
    fun connectsToTheWebsocketUsingTribe(): Unit = setup(object {
        val tribeId = TribeId("bwahahahaha")
        val useSsl = false
    }) exercise {
        shallow { couplingWebsocket(tribeId, useSsl, {}) { _ -> div {} } }
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
        shallow { couplingWebsocket(tribeId, useSsl, {}) { _ -> div {} } }
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
        val wrapper = shallow { couplingWebsocket(tribeId, false, { lastMessage = it }) { _ -> div {} } }
        val websocketProps = wrapper.find(reactWebsocket).props()
            .unsafeCast<WebsocketProps>()
        val expectedMessage = "Not connected"
    }) exercise {
        websocketProps.onMessage(socketMessage("lol"))
        wrapper.update()
        websocketProps.onClose()
        wrapper.update()
    } verify {
        lastMessage?.assertIsEqualTo(CouplingSocketMessage(expectedMessage, emptySet()))
    }

    private fun socketMessage(expectedMessage: String) = kotlinx.serialization.json.Json.encodeToString(
        CouplingSocketMessage(expectedMessage, emptySet(), null).toSerializable()
    )

}
