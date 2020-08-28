package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.client.external.reactwebsocket.WebsocketComponent
import com.zegreatrob.coupling.client.external.reactwebsocket.websocket
import com.zegreatrob.coupling.client.user.CouplingSocketMessage
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.minreact.reactFunction
import kotlinx.browser.window
import react.*
import react.dom.div
import kotlin.js.Json
import kotlin.js.json

val disconnectedMessage = json("text" to "Not connected", "players" to emptyArray<Json>())
    .unsafeCast<CouplingSocketMessage>()

fun RBuilder.couplingWebsocket(
    tribeId: TribeId,
    useSsl: Boolean,
    children: RBuilder.(CouplingSocketMessage, (Any) -> Unit) -> Unit
) = childFunction(
    CouplingWebsocket,
    CouplingWebsocketProps(tribeId, useSsl),
    {}) { (message, sendMessage): Pair<CouplingSocketMessage, (Any) -> Unit> ->
    children(message, sendMessage)
}

val CouplingWebsocket = reactFunction<CouplingWebsocketProps> { props ->
    val (tribeId, useSsl) = props
    val (message, setMessage) = useState(disconnectedMessage)
    val ref = useRef<WebsocketComponent?>(null)
    div {
        websocket {
            attrs {
                url = buildSocketUrl(tribeId, useSsl)
                onMessage = { setMessage(JSON.parse(it)) }
                onClose = { setMessage(disconnectedMessage) }
                this.ref = { ref.current = it }
            }
        }

        val sendMessageFunc = { it: Any ->
            val websocket = ref.current
            if (websocket != null)
                websocket.sendMessage(it)
            else
                console.error("Message not sent, websocket not initialized", it)
        }
        props.children(message to sendMessageFunc)
    }
}.unsafeCast<FunctionalComponent<CouplingWebsocketProps>>()

data class CouplingWebsocketProps(val tribeId: TribeId, val useSsl: Boolean) : RProps

private fun buildSocketUrl(tribeId: TribeId, useSsl: Boolean) =
    "${useSsl.protocol}://$host/api/${tribeId.value}/pairAssignments/current"

private val host get() = window.location.host
private val Boolean.protocol get() = if (this) "wss" else "ws"