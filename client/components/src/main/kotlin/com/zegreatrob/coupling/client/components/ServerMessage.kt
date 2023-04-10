package com.zegreatrob.coupling.client.components

import com.zegreatrob.coupling.model.CouplingSocketMessage
import com.zegreatrob.minreact.DataPropsBind
import com.zegreatrob.minreact.add
import com.zegreatrob.minreact.tmFC
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.span

data class ServerMessage(val message: CouplingSocketMessage) :
    DataPropsBind<ServerMessage>(serverMessage)

val serverMessage = tmFC<ServerMessage> { (message) ->
    div {
        span { +message.text }
        div {
            message.players.forEach {
                add(PlayerCard(it, size = 50))
            }
        }
    }
}
