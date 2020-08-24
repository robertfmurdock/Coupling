package com.zegreatrob.coupling.client.user

import com.zegreatrob.coupling.client.external.reactwebsocket.WebsocketComponent
import com.zegreatrob.coupling.client.external.reactwebsocket.websocket
import com.zegreatrob.coupling.client.player.PlayerCardProps
import com.zegreatrob.coupling.client.player.playerCard
import com.zegreatrob.coupling.json.toPlayer
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.minreact.reactFunction
import kotlinx.browser.window
import react.RProps
import react.dom.div
import react.dom.span
import react.useRef
import react.useState
import kotlin.js.Json
import kotlin.js.json

val disconnectedMessage = json("text" to "Not connected", "players" to emptyArray<Json>())
    .unsafeCast<WebsocketMessage>()

data class ServerMessageProps(val tribeId: TribeId, val useSsl: Boolean) : RProps

val ServerMessage = reactFunction<ServerMessageProps> { (tribeId, useSsl) ->
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
        span { +message.text }
        div {
            message.players.map { it.toPlayer() }
                .map { playerCard(PlayerCardProps(tribeId, it, size = 50)) }
        }
    }
}

interface WebsocketMessage {
    val text: String
    val players: Array<Json>
}

private fun buildSocketUrl(tribeId: TribeId, useSsl: Boolean): String {
    val protocol = if (useSsl) "wss" else "ws"
    return "$protocol://${window.location.host}/api/${tribeId.value}/pairAssignments/current"
}
