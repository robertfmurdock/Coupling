package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.client.external.reactwebsocket.WebsocketComponent
import com.zegreatrob.coupling.client.external.reactwebsocket.websocket
import com.zegreatrob.coupling.json.*
import com.zegreatrob.coupling.model.CouplingSocketMessage
import com.zegreatrob.coupling.model.Message
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.minreact.reactFunction
import kotlinx.browser.window
import org.w3c.dom.get
import org.w3c.dom.url.URL
import react.*
import react.dom.div

val disconnectedMessage = CouplingSocketMessage(
    text = "Not connected",
    players = emptySet(),
    currentPairAssignments = null
)

fun RBuilder.couplingWebsocket(
    tribeId: TribeId,
    useSsl: Boolean = "https:" == window.location.protocol,
    onMessage: (Message) -> Unit,
    children: RBuilder.(((Message) -> Unit)?) -> Unit
) = child(
    CouplingWebsocket,
    CouplingWebsocketProps(tribeId, useSsl, onMessage) { sendMessage: ((Message) -> Unit)? ->
        children(sendMessage)
    }
)

val CouplingWebsocket = reactFunction<CouplingWebsocketProps> { props ->
    val (tribeId, useSsl, onMessageFunc) = props

    var connected by useState(false)
    val ref = useRef<WebsocketComponent>(null)

    val sendMessageFunc = useMemo { sendMessageWithSocketFunc(ref) }

    div {
        websocket {
            attrs {
                url = buildSocketUrl(tribeId, useSsl).href
                onMessage = { onMessageFunc(it.fromJsonString<JsonMessage>().toModel()) }
                onOpen = { connected = true }
                onClose = { onMessageFunc(disconnectedMessage) }
                this.ref = { ref.current = it }
            }
        }

        props.children(this, if (connected) sendMessageFunc else null)
    }
}

private fun sendMessageWithSocketFunc(ref: RefObject<WebsocketComponent>) = { message: Message ->
    val websocket = ref.current
    if (websocket != null)
        message.toSerializable().toJsonString().let(websocket::sendMessage)
    else
        console.error("Message not sent, websocket not initialized", message)
}

data class CouplingWebsocketProps(
    val tribeId: TribeId,
    val useSsl: Boolean,
    val onMessage: (Message) -> Unit,
    val children: RBuilder.(value: ((Message) -> Unit)?) -> Unit
) : Props

private fun buildSocketUrl(tribeId: TribeId, useSsl: Boolean) = URL(
    "?tribeId=${encodeURIComponent(tribeId.value)}",
    "${useSsl.protocol}://$host"
)

external fun encodeURIComponent(value: String): String

private val host get() = window["websocketHost"].unsafeCast<String?>() ?: window.location.host
private val Boolean.protocol get() = if (this) "wss" else "ws"