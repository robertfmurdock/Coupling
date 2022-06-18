package com.zegreatrob.coupling.client.user

import com.zegreatrob.coupling.client.player.PlayerCard
import com.zegreatrob.coupling.model.CouplingSocketMessage
import com.zegreatrob.minreact.DataPropsBind
import com.zegreatrob.minreact.create
import com.zegreatrob.minreact.tmFC
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.span

data class ServerMessage(val message: CouplingSocketMessage) :
    DataPropsBind<ServerMessage>(serverMessage)

val serverMessage = tmFC<ServerMessage> { (message) ->
    div {
        span { +message.text }
        div {
            message.players.map { +PlayerCard(it, size = 50).create() }
        }
    }
}
