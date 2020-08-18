package com.zegreatrob.coupling.client.user

import com.zegreatrob.coupling.client.player.PlayerCardProps
import com.zegreatrob.coupling.client.player.playerCard
import com.zegreatrob.coupling.json.toPlayer
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.minreact.reactFunction
import react.RClass
import react.RProps
import react.dom.div
import react.dom.span
import react.useState
import kotlinx.browser.window
import kotlin.js.Json
import kotlin.js.json

@JsModule("react-websocket")
private external val websocket: RClass<WebsocketProps>

external interface WebsocketProps : RProps {
    var url: String
    var onMessage: (String) -> Unit
    var onClose: () -> Unit
}

val disconnectedMessage = json("text" to "Not connected", "players" to emptyArray<Json>())
    .unsafeCast<WebsocketMessage>()

data class ServerMessageProps(val tribeId: TribeId, val useSsl: Boolean) : RProps

val ServerMessage = reactFunction<ServerMessageProps> { (tribeId, useSsl) ->
    val (message, setMessage) = useState(disconnectedMessage)
    div {
        websocket {
            attrs {
                url = buildSocketUrl(tribeId, useSsl)
                onMessage = { setMessage(JSON.parse(it)) }
                onClose = { setMessage(disconnectedMessage) }
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
