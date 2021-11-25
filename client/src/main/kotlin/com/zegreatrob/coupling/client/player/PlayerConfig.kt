package com.zegreatrob.coupling.client.player

import com.zegreatrob.coupling.client.DispatchFunc
import com.zegreatrob.coupling.client.configFrame
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.minreact.child
import com.zegreatrob.minreact.reactFunction
import kotlinx.css.*
import kotlinx.css.Color.Companion.wheat
import react.Props
import react.dom.div

data class PlayerConfigProps(
    val tribe: Tribe,
    val player: Player,
    val players: List<Player>,
    val reload: () -> Unit,
    val dispatchFunc: DispatchFunc<out PlayerConfigDispatcher>
) : Props

private val styles = useStyles("player/PlayerConfig")

val PlayerConfig = reactFunction { (tribe, player, players, reload, commandFunc): PlayerConfigProps ->
    configFrame(styles.className) {
        child(PlayerConfigEditor, PlayerConfigEditoProps(tribe, player, reload, commandFunc))
        div {
            child(
                PlayerRoster, PlayerRosteProps(
                    players = players,
                    tribeId = tribe.id
                ) {
                    display = Display.inlineBlock
                    borderRadius = 20.px
                    padding = "10px"
                    border = "11px outset tan"
                    backgroundColor = wheat
                }
            )
        }
    }
}
