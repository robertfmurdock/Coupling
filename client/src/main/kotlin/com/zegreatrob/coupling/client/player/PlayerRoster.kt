package com.zegreatrob.coupling.client.player

import com.zegreatrob.coupling.client.external.react.get
import com.zegreatrob.coupling.client.external.react.reactFunction
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.tribe.TribeId
import kotlinx.html.classes
import react.RBuilder
import react.RProps
import react.dom.button
import react.dom.div
import react.router.dom.routeLink

val RBuilder.playerRoster get() = PlayerRoster.render(this)

data class PlayerRosterProps(
    val label: String? = null,
    val players: List<Player>,
    val tribeId: TribeId,
    val pathSetter: (String) -> Unit,
    val className: String? = null
) : RProps

private val styles = useStyles("player/PlayerRoster")

val PlayerRoster = reactFunction<PlayerRosterProps> { (label, players, tribeId, pathSetter, className) ->
    div(classes = className) {
        attrs { classes += styles.className }
        div {
            div(classes = styles["header"]) {
                +(label ?: "Players")
            }
            renderPlayers(players, tribeId, pathSetter)
        }
        addPlayerButton(tribeId)
    }
}

private fun RBuilder.addPlayerButton(tribeId: TribeId) = routeLink(to = "/${tribeId.value}/player/new/") {
    button(classes = "large orange button") {
        attrs { classes += styles["addPlayerButton"] }
        +"Add a new player!"
    }
}

private fun RBuilder.renderPlayers(players: List<Player>, tribeId: TribeId, pathSetter: (String) -> Unit) =
    players.forEach { player ->
        playerCard(
            PlayerCardProps(tribeId, player, pathSetter),
            key = player.id
        )
    }
