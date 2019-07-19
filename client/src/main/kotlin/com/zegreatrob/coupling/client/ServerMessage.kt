package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.common.entity.tribe.TribeId
import react.RClass
import react.RProps
import react.dom.div
import react.dom.span
import kotlin.browser.window

@JsModule("react-websocket")
@JsNonModule
private external val websocket: RClass<WebsocketProps>

external interface WebsocketProps : RProps {
    var url: String
    var onMessage: (String) -> Unit
    var onClose: () -> Unit
}

const val disconnectedMessage = "Not connected"

data class ServerMessageProps(val tribeId: TribeId, val useSsl: Boolean) : RProps

interface ServerMessageRenderer {
    companion object {
        val serverMessage = reactFunctionComponent<ServerMessageProps> { props ->
            val (tribeId, useSsl) = props
            val (message, setMessage) = useState(disconnectedMessage)

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
