package com.zegreatrob.coupling.client.player

import com.zegreatrob.coupling.client.child
import com.zegreatrob.coupling.client.dom.CouplingButton
import com.zegreatrob.coupling.client.dom.large
import com.zegreatrob.coupling.client.dom.orange
import com.zegreatrob.coupling.client.external.react.get
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.client.reactFunction
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.minreact.DataProps
import com.zegreatrob.minreact.TMFC
import kotlinx.css.RuleSet
import kotlinx.html.classes
import react.RBuilder
import react.dom.attrs
import react.dom.div
import react.router.dom.Link
import styled.css
import styled.styledDiv

data class PlayerRoster(
    val label: String? = null,
    val players: List<Player>,
    val tribeId: TribeId,
    val className: String? = null,
    val cssOverrides: RuleSet = {}
) : DataProps<PlayerRoster> {
    override val component: TMFC<PlayerRoster> get() = playerRoster
}

private val styles = useStyles("player/PlayerRoster")

val playerRoster = reactFunction { (label, players, tribeId, className, overrides): PlayerRoster ->
    styledDiv {
        attrs {
            if (className != null) classes = classes + className
            classes = classes + styles.className
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

private fun RBuilder.addPlayerButton(tribeId: TribeId) = Link {
    attrs.to = "/${tribeId.value}/player/new/"
    child(CouplingButton(large, orange, styles["addPlayerButton"], {}, {}, fun RBuilder.() {
 +"Add a new player!"
}))
}

private fun RBuilder.renderPlayers(players: List<Player>, tribeId: TribeId) = players.forEach { player ->
    child(
        PlayerCard(tribeId, player, linkToConfig = true),
        key = player.id
    )
}
