package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.common.entity.player.Player
import kotlinx.html.classes
import kotlinx.html.id
import react.RBuilder
import react.RProps
import react.dom.a
import react.dom.div


data class PlayerRosterProps(
        val label: String?,
        val players: List<Player>,
        val tribeId: String,
        val pathSetter: (String) -> Unit,
        val className: String?
) : RProps

val playerRoster = rFunction { props: PlayerRosterProps ->
    div {
        attrs {
            classes = setOf("react-player-roster", props.className ?: "")
        }
        div {
            div(classes = "roster-header") {
                +(props.label ?: "Players")
            }
            renderPlayers(props)
        }
        a(href = "/${props.tribeId}/player/new/", classes = "large orange button") {
            attrs {
                id = "add-player-button"
            }

            +"Add a new player!"
        }
    }
}

private fun RBuilder.renderPlayers(props: PlayerRosterProps) {
    props.players.forEach { player ->
        child(
                type = playerCard,
                props = PlayerCardProps(
                        tribeId = props.tribeId,
                        player = player,
                        className = null,
                        pathSetter = props.pathSetter
                )) {
            attrs {
                key = player.id ?: ""
            }
        }
    }
}
