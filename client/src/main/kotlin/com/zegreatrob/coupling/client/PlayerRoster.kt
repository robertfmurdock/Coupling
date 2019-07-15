package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.common.entity.player.Player
import com.zegreatrob.coupling.common.entity.tribe.TribeId
import kotlinx.html.classes
import loadStyles
import react.RBuilder
import react.RProps
import react.dom.a
import react.dom.div

private val styles: PlayerRosterStyles = loadStyles("PlayerRoster")

interface PlayerRosterStyles {
    val className: String
    val addPlayerButton: String
    val header: String
}

data class PlayerRosterProps(
        val label: String?,
        val players: List<Player>,
        val tribeId: TribeId,
        val pathSetter: (String) -> Unit,
        val className: String?
) : RProps

interface PlayerRosterRenderer : PlayerCardRenderer {

    val RBuilder.playerRoster
        get() = rFunction { props: PlayerRosterProps ->
            with(props) {
                div(classes = className) {
                    attrs { classes += styles.className }
                    div {
                        div(classes = styles.header) {
                            +(label ?: "Players")
                        }
                        renderPlayers(props)
                    }
                    a(href = "/${tribeId.value}/player/new/", classes = "large orange button") {
                        attrs { classes += styles.addPlayerButton }
                        +"Add a new player!"
                    }
                }
            }
        }

    private fun RBuilder.renderPlayers(props: PlayerRosterProps) = with(props) {
        players.forEach { player ->
            element(
                    playerCard,
                    PlayerCardProps(tribeId = tribeId, player = player, pathSetter = pathSetter),
                    key = player.id
            )
        }
    }

}
