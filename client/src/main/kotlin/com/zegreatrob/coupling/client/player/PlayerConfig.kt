package com.zegreatrob.coupling.client.player

import com.zegreatrob.coupling.client.DispatchFunc
import com.zegreatrob.coupling.client.child
import com.zegreatrob.coupling.client.configFrame
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.client.reactFunction
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.minreact.DataProps
import kotlinx.css.*
import kotlinx.css.Color.Companion.wheat
import react.dom.div

data class PlayerConfig(
    val tribe: Tribe,
    val player: Player,
    val players: List<Player>,
    val reload: () -> Unit,
    val dispatchFunc: DispatchFunc<out PlayerConfigDispatcher>
) : DataProps<PlayerConfig> {
    override val component get() = playerConfig
}

private val styles = useStyles("player/PlayerConfig")

val playerConfig = reactFunction { (tribe, player, players, reload, commandFunc): PlayerConfig ->
    configFrame(styles.className) {
        child(PlayerConfigEditor(tribe, player, reload, commandFunc))
        div {
            child(PlayerRoster(players = players, tribeId = tribe.id) {
                display = Display.inlineBlock
                borderRadius = 20.px
                padding = "10px"
                border = "11px outset tan"
                backgroundColor = wheat
            })
        }
    }
}
