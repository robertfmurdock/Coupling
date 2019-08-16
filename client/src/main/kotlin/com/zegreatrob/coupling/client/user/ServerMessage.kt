package com.zegreatrob.coupling.client.user

import com.zegreatrob.coupling.client.external.react.*
import com.zegreatrob.coupling.common.entity.tribe.TribeId
import react.RBuilder
import react.RClass
import react.RProps
import react.ReactElement
import react.dom.div
import react.dom.span
import kotlin.browser.window

@JsModule("react-websocket")
private external val websocket: RClass<WebsocketProps>

external interface WebsocketProps : RProps {
    var url: String
    var onMessage: (String) -> Unit
    var onClose: () -> Unit
}

const val disconnectedMessage = "Not connected"

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
                        onMessage = { setMessage(it) }
                        onClose = { setMessage(disconnectedMessage) }
                    }
                }

                span {
                    +message
                }
            }
        }
    }
}

private fun buildSocketUrl(tribeId: TribeId, useSsl: Boolean): String {
    val protocol = if (useSsl) "wss" else "ws"
    return "$protocol://${window.location.host}/api/${tribeId.value}/pairAssignments/current"
}
