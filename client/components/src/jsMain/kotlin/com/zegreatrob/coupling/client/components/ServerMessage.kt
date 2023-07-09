package com.zegreatrob.coupling.client.components

import com.zegreatrob.coupling.client.components.player.PlayerCard
import com.zegreatrob.coupling.model.CouplingSocketMessage
import com.zegreatrob.minreact.DataPropsBind
import com.zegreatrob.minreact.add
import com.zegreatrob.minreact.ntmFC
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.span

data class ServerMessage(val message: CouplingSocketMessage) :
    DataPropsBind<ServerMessage>(serverMessage)

val serverMessage by ntmFC<ServerMessage> { (message) ->
    div {
        span { +message.text }
        div {
            message.players.forEach {
                add(PlayerCard(it, size = 50))
            }
        }
    }
}
