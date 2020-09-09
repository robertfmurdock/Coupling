package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.client.external.reactwebsocket.WebsocketComponent
import com.zegreatrob.coupling.client.external.reactwebsocket.websocket
import com.zegreatrob.coupling.client.user.CouplingSocketMessage
import com.zegreatrob.coupling.json.toPairAssignmentDocument
import com.zegreatrob.coupling.json.toPlayer
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.minreact.reactFunction
import kotlinx.browser.window
import react.*
import react.dom.div
import kotlin.js.Json

val disconnectedMessage = CouplingSocketMessage(
    text = "Not connected",
    players = emptyList(),
    currentPairAssignments = null
)

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

    val sendMessageFunc = useMemo({ sendMessageWithSocketFunc(ref) }, emptyArray())

    div {
        websocket {
            attrs {
                url = buildSocketUrl(tribeId, useSsl)
                onMessage = { setMessage(toCouplingServerMessage(JSON.parse(it))) }
                onClose = { setMessage(disconnectedMessage) }
                this.ref = { ref.current = it }
            }
        }

        props.children(message to sendMessageFunc)
    }
}.unsafeCast<FunctionalComponent<CouplingWebsocketProps>>()

private fun sendMessageWithSocketFunc(ref: RMutableRef<WebsocketComponent?>) = { message: Any ->
    val websocket = ref.current
    if (websocket != null)
        websocket.sendMessage(message)
    else
        console.error("Message not sent, websocket not initialized", message)
}

private fun toCouplingServerMessage(json: Json) = CouplingSocketMessage(
    json["text"].toString(),
    json["players"].unsafeCast<Array<Json>>().map { it.toPlayer() },
    json["currentPairAssignments"].unsafeCast<Json?>()?.toPairAssignmentDocument()
)

data class CouplingWebsocketProps(val tribeId: TribeId, val useSsl: Boolean) : RProps

private fun buildSocketUrl(tribeId: TribeId, useSsl: Boolean) =
    "${useSsl.protocol}://$host/api/${tribeId.value}/pairAssignments/current"

private val host get() = window.location.host
private val Boolean.protocol get() = if (this) "wss" else "ws"