package com.zegreatrob.coupling.client.player

import com.zegreatrob.coupling.client.external.react.get
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.minreact.reactFunction
import kotlinx.css.RuleSet
import kotlinx.html.classes
import react.RBuilder
import react.RProps
import react.dom.button
import react.dom.div
import react.router.dom.routeLink
import styled.css
import styled.styledDiv

data class PlayerRosterProps(
    val label: String? = null,
    val players: List<Player>,
    val tribeId: TribeId,
    val pathSetter: (String) -> Unit,
    val className: String? = null,
    val cssOverrides: RuleSet = {}
) : RProps

private val styles = useStyles("player/PlayerRoster")

val PlayerRoster = reactFunction { (label, players, tribeId, pathSetter, className, overrides): PlayerRosterProps ->
    styledDiv {
        attrs {
            if (className != null) classes += className
            classes += styles.className
        }
        css { overrides() }
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
