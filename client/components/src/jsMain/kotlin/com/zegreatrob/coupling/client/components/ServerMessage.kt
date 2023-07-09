package com.zegreatrob.coupling.client.components

import com.zegreatrob.coupling.client.components.player.PlayerCard
import com.zegreatrob.coupling.model.CouplingSocketMessage
import com.zegreatrob.minreact.ReactFunc
import com.zegreatrob.minreact.nfc
import react.Props
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.span

external interface ServerMessageProps : Props {
    var message: CouplingSocketMessage
}

@ReactFunc
val ServerMessage by nfc<ServerMessageProps> { (message) ->
    div {
        span { +message.text }
        div {
            message.players.forEach { PlayerCard(it, size = 50) }
        }
    }
}
