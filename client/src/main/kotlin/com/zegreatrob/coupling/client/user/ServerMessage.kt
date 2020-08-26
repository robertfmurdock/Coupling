package com.zegreatrob.coupling.client.user

import com.zegreatrob.coupling.client.external.reactwebsocket.WebsocketComponent
import com.zegreatrob.coupling.client.external.reactwebsocket.websocket
import com.zegreatrob.coupling.client.player.PlayerCardProps
import com.zegreatrob.coupling.client.player.playerCard
import com.zegreatrob.coupling.json.toPlayer
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.minreact.reactFunction
import kotlinx.browser.window
import react.*
import react.dom.div
import react.dom.span
import kotlin.js.Json
import kotlin.js.json

val disconnectedMessage = json("text" to "Not connected", "players" to emptyArray<Json>())
    .unsafeCast<CouplingSocketMessage>()

data class ServerMessageProps(val tribeId: TribeId, val useSsl: Boolean) : RProps

val ServerMessage = reactFunction<ServerMessageProps> { (tribeId, useSsl) ->
    couplingWebsocket(tribeId, useSsl) { message, _ ->
        span { +message.text }
        div {
            message.players.map { it.toPlayer() }
                .map { playerCard(PlayerCardProps(tribeId, it, size = 50)) }
        }
    }
}

private fun RBuilder.couplingWebsocket(
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

external interface CouplingSocketMessage {
    val text: String
    val players: Array<Json>
}

private fun buildSocketUrl(tribeId: TribeId, useSsl: Boolean) =
    "${useSsl.protocol}://$host/api/${tribeId.value}/pairAssignments/current"

private val host get() = window.location.host

private val Boolean.protocol get() = if (this) "wss" else "ws"
