package com.zegreatrob.coupling.client.player

import com.zegreatrob.coupling.client.dom.couplingButton
import com.zegreatrob.coupling.client.dom.large
import com.zegreatrob.coupling.client.dom.orange
import com.zegreatrob.coupling.client.external.react.get
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.minreact.reactFunction
import kotlinx.css.RuleSet
import kotlinx.html.classes
import react.RBuilder
import react.RProps
import react.dom.div
import react.router.dom.routeLink
import styled.css
import styled.styledDiv

data class PlayerRosterProps(
    val label: String? = null,
    val players: List<Player>,
    val tribeId: TribeId,
    val className: String? = null,
    val cssOverrides: RuleSet = {}
) : RProps

private val styles = useStyles("player/PlayerRoster")

val PlayerRoster = reactFunction { (label, players, tribeId, className, overrides): PlayerRosterProps ->
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
            renderPlayers(players, tribeId)
        }
        addPlayerButton(tribeId)
    }
}

private fun RBuilder.addPlayerButton(tribeId: TribeId) = routeLink(to = "/${tribeId.value}/player/new/") {
    couplingButton(large, orange, styles["addPlayerButton"]) {
        +"Add a new player!"
    }
}

private fun RBuilder.renderPlayers(players: List<Player>, tribeId: TribeId) = players.forEach { player ->
    playerCard(
        PlayerCardProps(tribeId, player, linkToConfig = true),
        key = player.id
    )
}
