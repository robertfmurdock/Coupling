package com.zegreatrob.coupling.client.components

import com.zegreatrob.coupling.client.components.external.reactwebsocket.UseWebSocketOptions
import com.zegreatrob.coupling.client.components.external.reactwebsocket.useWebSocket
import com.zegreatrob.coupling.json.JsonMessage
import com.zegreatrob.coupling.json.fromJsonString
import com.zegreatrob.coupling.json.toJsonString
import com.zegreatrob.coupling.json.toModel
import com.zegreatrob.coupling.json.toSerializable
import com.zegreatrob.coupling.model.Message
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.minreact.ReactFunc
import com.zegreatrob.minreact.nfc
import kotlinx.browser.window
import org.w3c.dom.get
import org.w3c.dom.url.URL
import react.Props
import react.ReactNode
import react.dom.html.ReactHTML.div
import react.useMemo
import react.useState

val disconnectedMessage = com.zegreatrob.coupling.model.CouplingSocketMessage(
    text = "Not connected",
    players = emptySet(),
    currentPairAssignments = null,
)

external interface CouplingWebsocketProps : Props {
    @Suppress("INLINE_CLASS_IN_EXTERNAL_DECLARATION_WARNING")
    var partyId: PartyId
    var onMessage: (Message) -> Unit
    var buildChild: (value: ((Message) -> Unit)?) -> ReactNode
    var token: String
    var useSsl: Boolean?
}

@ReactFunc
val CouplingWebsocket by nfc<CouplingWebsocketProps> { props ->
    val (partyId, onMessageFunc, buildChild, token) = props
    val useSsl = props.useSsl ?: "https:" == window.location.protocol
    var connected by useState(false)

    val socketHook = useWebSocket(
        url = buildSocketUrl(partyId, useSsl, token).href,
        UseWebSocketOptions(
            onMessage = { (it.data as? String)?.fromJsonString<JsonMessage>()?.toModel()?.let(onMessageFunc) },
            onOpen = { connected = true },
            onClose = { onMessageFunc(disconnectedMessage) },
        ),
    )

    val sendMessageFunc: (Message) -> Unit = useMemo { sendMessageWithSocketFunc(socketHook.sendMessage) }

    div {
        +buildChild(if (connected) sendMessageFunc else null)
    }
}

private fun sendMessageWithSocketFunc(sendMessage: (message: String, keep: Boolean) -> Unit) = { message: Message ->
    message.toSerializable().toJsonString().let { sendMessage(it, true) }
}

private fun buildSocketUrl(partyId: PartyId, useSsl: Boolean, token: String) = URL(
    "?partyId=${encodeURIComponent(partyId.value)}&token=${encodeURIComponent(token)}",
    "${useSsl.protocol}://$host",
)

external fun encodeURIComponent(value: String): String
private val host get() = window["websocketHost"].unsafeCast<String?>() ?: window.location.host
private val Boolean.protocol get() = if (this) "wss" else "ws"
