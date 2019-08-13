package com.zegreatrob.coupling.client.player

import com.zegreatrob.coupling.client.external.react.ComponentProvider
import com.zegreatrob.coupling.client.external.react.StyledComponentBuilder
import com.zegreatrob.coupling.client.external.react.buildBy
import com.zegreatrob.coupling.client.external.react.reactElement
import com.zegreatrob.coupling.common.entity.player.Player
import com.zegreatrob.coupling.common.entity.tribe.TribeId
import kotlinx.html.classes
import react.RBuilder
import react.RProps
import react.dom.a
import react.dom.div


object PlayerRoster : ComponentProvider<PlayerRosterProps>(), PlayerRosterBuilder

val RBuilder.playerRoster get() = PlayerRoster.captor(this)

interface PlayerRosterStyles {
    val className: String
    val addPlayerButton: String
    val header: String
}

data class PlayerRosterProps(
    val label: String? = null,
    val players: List<Player>,
    val tribeId: TribeId,
    val pathSetter: (String) -> Unit,
    val className: String? = null
) : RProps

interface PlayerRosterBuilder : StyledComponentBuilder<PlayerRosterProps, PlayerRosterStyles> {

    override val componentPath: String get() = "player/PlayerRoster"
    override fun build() = buildBy {
        reactElement {
            div(classes = props.className) {
                attrs { classes += styles.className }
                div {
                    div(classes = styles.header) {
                        +(props.label ?: "Players")
                    }
                    renderPlayers(props)
                }
                a(href = "/${props.tribeId.value}/player/new/", classes = "large orange button") {
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

val RBuilder.pairAssignments get() = PlayerRoster.captor(this)
