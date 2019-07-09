package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.common.entity.player.Player
import kotlinx.html.classes
import react.RBuilder
import react.RProps
import react.dom.div
import react.key


external interface PlayerRosterProps : RProps {
    var label: String?
    var players: List<Player>
    var tribeId: String
    var pathSetter: (String) -> Unit
    var className: String?
}

val playerRoster = rFunction { props: PlayerRosterProps ->
    div {
        attrs {
            classes = setOf("react-player-roster", props.className ?: "")
        }
        div(classes = "roster-header") {
            +(props.label ?: "Players")
        }
        renderPlayers(props)
    }
}

private fun RBuilder.renderPlayers(props: PlayerRosterProps) {
    props.players.forEach { player ->
        playerCard {
            attrs {
                this.key = player.id ?: ""
                this.tribeId = props.tribeId
                this.player = player
                this.pathSetter = props.pathSetter
                this.disabled = false
            }
        }
    }
}
