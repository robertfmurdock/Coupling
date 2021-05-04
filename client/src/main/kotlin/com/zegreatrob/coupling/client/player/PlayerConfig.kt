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
import react.RProps
import react.dom.div

data class PlayerConfigProps(
    val tribe: Tribe,
    val player: Player,
    val players: List<Player>,
    val pathSetter: (String) -> Unit,
    val reload: () -> Unit,
    val dispatchFunc: DispatchFunc<out PlayerConfigDispatcher>
) : RProps

private val styles = useStyles("player/PlayerConfig")

val PlayerConfig = reactFunction { (tribe, player, players, pathSetter, reload, commandFunc): PlayerConfigProps ->
    configFrame(styles.className) {
        child(PlayerConfigEditor, PlayerConfigEditorProps(tribe, player, reload, commandFunc))
        div {
            child(
                PlayerRoster, PlayerRosterProps(
                    players = players,
                    tribeId = tribe.id,
                    pathSetter = pathSetter,
                    cssOverrides = {
                        display = Display.inlineBlock
                        borderRadius = 20.px
                        padding = "10px"
                        border = "11px outset tan"
                        backgroundColor = wheat
                    }
                )
            )
        }
    }
}
