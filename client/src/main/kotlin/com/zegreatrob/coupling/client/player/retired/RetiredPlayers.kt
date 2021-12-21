package com.zegreatrob.coupling.client.player.retired

import com.zegreatrob.coupling.client.child
import com.zegreatrob.coupling.client.external.react.get
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.client.player.PlayerCardProps
import com.zegreatrob.coupling.client.player.playerCard
import com.zegreatrob.coupling.client.reactFunction
import com.zegreatrob.coupling.client.tribe.TribeBrowser
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.minreact.DataProps
import com.zegreatrob.minreact.TMFC
import react.dom.div

data class RetiredPlayers(val tribe: Tribe, val retiredPlayers: List<Player>) : DataProps<RetiredPlayers> {
    override val component: TMFC<RetiredPlayers> = com.zegreatrob.coupling.client.player.retired.retiredPlayers
}

private val styles = useStyles("player/RetiredPlayers")

val retiredPlayers = reactFunction<RetiredPlayers> { (tribe, players) ->
    div(classes = styles.className) {
        child(TribeBrowser(tribe))
        div(classes = styles["header"]) { +"Retired Players" }
        div {
            players.forEach { player ->
                playerCard(
                    PlayerCardProps(tribe.id, player, linkToConfig = true, deselected = true),
                    key = player.id
                )
            }
        }
    }
}
