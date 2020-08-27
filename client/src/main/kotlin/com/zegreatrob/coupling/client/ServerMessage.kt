package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.client.external.reactwebsocket.WebsocketComponent
import com.zegreatrob.coupling.client.external.reactwebsocket.websocket
import com.zegreatrob.coupling.client.user.CouplingSocketMessage
import com.zegreatrob.coupling.model.tribe.TribeId
import kotlinx.browser.window
import react.RBuilder
import react.ReactElement
import react.dom.div
import react.useRef
import react.useState
import kotlin.js.Json
import kotlin.js.json

val disconnectedMessage = json("text" to "Not connected", "players" to emptyArray<Json>())
    .unsafeCast<CouplingSocketMessage>()

fun RBuilder.couplingWebsocket(
    tribeId: TribeId,
    useSsl: Boolean,
    children: RBuilder.(CouplingSocketMessage, (Any) -> Unit) -> Unit
): ReactElement {
    val (message, setMessage) = useState(disconnectedMessage)
    val ref = useRef<WebsocketComponent?>(null)
    return div {
        websocket {
            attrs {
                url = buildSocketUrl(tribeId, useSsl)
                onMessage = { setMessage(JSON.parse(it)) }
                onClose = { setMessage(disconnectedMessage) }
                this.ref = { ref.current = it }
            }
        }
        children(message) {
            val websocket = ref.current
            if (websocket != null)
                websocket.sendMessage(it)
            else
                console.error("Message not sent, websocket not initialized", it)
        }
    }
}

private fun buildSocketUrl(tribeId: TribeId, useSsl: Boolean) =
    "${useSsl.protocol}://$host/api/${tribeId.value}/pairAssignments/current"

private val host get() = window.location.host
private val Boolean.protocol get() = if (this) "wss" else "ws"