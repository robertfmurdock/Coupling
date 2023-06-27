package com.zegreatrob.coupling.client.components

import com.zegreatrob.coupling.client.components.external.reactwebsocket.useWebSocket
import com.zegreatrob.coupling.json.JsonMessage
import com.zegreatrob.coupling.json.fromJsonString
import com.zegreatrob.coupling.json.toJsonString
import com.zegreatrob.coupling.json.toModel
import com.zegreatrob.coupling.json.toSerializable
import com.zegreatrob.coupling.model.Message
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.minreact.DataPropsBind
import com.zegreatrob.minreact.ntmFC
import js.core.jso
import kotlinx.browser.window
import org.w3c.dom.get
import org.w3c.dom.url.URL
import react.ReactNode
import react.dom.html.ReactHTML.div
import react.useMemo
import react.useState

val disconnectedMessage = com.zegreatrob.coupling.model.CouplingSocketMessage(
    text = "Not connected",
    players = emptySet(),
    currentPairAssignments = null,
)
val couplingWebsocket by ntmFC<CouplingWebsocket> { props ->
    val (partyId, useSsl, onMessageFunc, buildChild, token) = props
    var connected by useState(false)

    val socketHook = useWebSocket(
        url = buildSocketUrl(partyId, useSsl, token).href,
        jso {
            onMessage = { (it.data as? String)?.fromJsonString<JsonMessage>()?.toModel()?.let(onMessageFunc) }
            onOpen = { connected = true }
            onClose = { onMessageFunc(disconnectedMessage) }
        },
    )

    val sendMessageFunc: (Message) -> Unit = useMemo { sendMessageWithSocketFunc(socketHook.sendMessage) }

    div {
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
