package com.zegreatrob.coupling.client.user

import com.zegreatrob.coupling.client.external.react.*
import com.zegreatrob.coupling.client.player.PlayerCardProps
import com.zegreatrob.coupling.client.player.playerCard
import com.zegreatrob.coupling.core.entity.tribe.TribeId
import com.zegreatrob.coupling.core.json.toPlayer
import react.RBuilder
import react.RClass
import react.RProps
import react.ReactElement
import react.dom.div
import react.dom.span
import kotlin.browser.window
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

object ServerMessage : RComponent<ServerMessageProps>(provider()), ServerMessageRenderer

val RBuilder.serverMessage get() = ServerMessage.render(this)

data class ServerMessageProps(val tribeId: TribeId, val useSsl: Boolean) : RProps

interface ServerMessageRenderer : SimpleComponentRenderer<ServerMessageProps> {

    override fun RContext<ServerMessageProps>.render(): ReactElement {
        val (tribeId, useSsl) = props
        val (message, setMessage) = useState(disconnectedMessage)

        return reactElement {
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
                        .map { playerCard(PlayerCardProps(tribeId, it, size = 50, pathSetter = {})) }
                }
            }
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
