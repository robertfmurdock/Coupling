package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.common.entity.player.Player
import com.zegreatrob.coupling.common.entity.tribe.TribeId
import kotlinx.html.classes
import kotlinx.html.id
import react.RBuilder
import react.RProps
import react.dom.a
import react.dom.div


data class PlayerRosterProps(
        val label: String?,
        val players: List<Player>,
        val tribeId: TribeId,
        val pathSetter: (String) -> Unit,
        val className: String?
) : RProps

val playerRoster = rFunction { props: PlayerRosterProps ->
    with(props) {
        div {
            attrs {
                classes = setOf("react-player-roster", className ?: "")
            }
            div {
                div(classes = "roster-header") {
                    +(label ?: "Players")
                }
                renderPlayers(props)
            }
            a(href = "/${tribeId.value}/player/new/", classes = "large orange button") {
                attrs {
                    id = "add-player-button"
                }

                +"Add a new player!"
            }
        }
    }
}

fun RBuilder.renderPlayers(props: PlayerRosterProps) = with(props) {
    players.forEach { player ->
        element(
                playerCard,
                PlayerCardProps(
                        tribeId = tribeId,
                        player = player,
                        className = null,
                        pathSetter = pathSetter
                )
        ) {
            attrs {
                key = player.id ?: ""
            }
        }
    }
}
