package com.zegreatrob.coupling.client.player

import com.zegreatrob.coupling.client.component
import com.zegreatrob.coupling.client.reactFunctionComponent
import com.zegreatrob.coupling.common.entity.player.Player
import com.zegreatrob.coupling.common.entity.tribe.TribeId
import kotlinx.html.classes
import loadStyles
import react.RBuilder
import react.RProps
import react.dom.a
import react.dom.div

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

interface PlayerRosterRenderer {

    fun RBuilder.playerRoster(props: PlayerRosterProps) = component(playerRoster, props)

    companion object : PlayerCardRenderer {
        private val styles: PlayerRosterStyles = loadStyles("player/PlayerRoster")

        private val playerRoster = reactFunctionComponent { props: PlayerRosterProps ->
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
                playerCard(
                        PlayerCardProps(tribeId = tribeId, player = player, pathSetter = pathSetter),
                        key = player.id
                )
            }
        }
    }
}
