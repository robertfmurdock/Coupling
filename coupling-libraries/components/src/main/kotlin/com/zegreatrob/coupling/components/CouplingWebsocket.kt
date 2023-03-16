package com.zegreatrob.coupling.components

import com.zegreatrob.coupling.components.external.reactwebsocket.WebsocketComponent
import com.zegreatrob.coupling.components.external.reactwebsocket.websocket
import com.zegreatrob.coupling.json.JsonMessage
import com.zegreatrob.coupling.json.fromJsonString
import com.zegreatrob.coupling.json.toJsonString
import com.zegreatrob.coupling.json.toModel
import com.zegreatrob.coupling.json.toSerializable
import com.zegreatrob.coupling.model.Message
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.minreact.DataPropsBind
import com.zegreatrob.minreact.tmFC
import kotlinx.browser.window
import org.w3c.dom.get
import org.w3c.dom.url.URL
import react.ReactNode
import react.RefObject
import react.dom.html.ReactHTML.div
import react.useMemo
import react.useRef
import react.useState

val disconnectedMessage = com.zegreatrob.coupling.model.CouplingSocketMessage(
    text = "Not connected",
    players = emptySet(),
    currentPairAssignments = null,
)
val couplingWebsocket = tmFC<CouplingWebsocket> { props ->
    val (partyId, useSsl, onMessageFunc, buildChild, token) = props

    var connected by useState(false)
    val ref = useRef<WebsocketComponent>(null)

    val sendMessageFunc = useMemo { sendMessageWithSocketFunc(ref) }

    div {
        websocket {
            url = buildSocketUrl(partyId, useSsl, token).href
            onMessage = { onMessageFunc(it.fromJsonString<JsonMessage>().toModel()) }
            onOpen = { connected = true }
            onClose = { onMessageFunc(disconnectedMessage) }
            this.ref = { ref.current = it }
        }

        +buildChild(if (connected) sendMessageFunc else null)
    }
}

data class CouplingWebsocket(
    val partyId: PartyId,
    val useSsl: Boolean = "https:" == window.location.protocol,
    val onMessage: (Message) -> Unit,
    val buildChild: (value: ((Message) -> Unit)?) -> ReactNode,
    val token: String,
) : DataPropsBind<CouplingWebsocket>(couplingWebsocket)

private fun sendMessageWithSocketFunc(ref: RefObject<WebsocketComponent>) = { message: Message ->
    val websocket = ref.current
    if (websocket != null) {
        message.toSerializable().toJsonString().let(websocket::sendMessage)
    } else {
        console.error("Message not sent, websocket not initialized", message)
    }
}

private fun buildSocketUrl(partyId: PartyId, useSsl: Boolean, token: String) = URL(
    "?partyId=${encodeURIComponent(partyId.value)}&token=${encodeURIComponent(token)}",
    "${useSsl.protocol}://$host",
)

external fun encodeURIComponent(value: String): String
private val host get() = window["websocketHost"].unsafeCast<String?>() ?: window.location.host
private val Boolean.protocol get() = if (this) "wss" else "ws"
