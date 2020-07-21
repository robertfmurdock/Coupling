package com.zegreatrob.coupling.client.player

import com.zegreatrob.coupling.client.DispatchFunc
import com.zegreatrob.coupling.client.configFrame
import com.zegreatrob.coupling.client.external.react.child
import com.zegreatrob.coupling.client.external.react.get
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.minreact.reactFunction
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

val PlayerConfig = reactFunction<PlayerConfigProps> { (tribe, player, players, pathSetter, reload, commandFunc) ->
    configFrame(styles.className) {
        child(PlayerConfigEditor, PlayerConfigEditorProps(tribe, player, pathSetter, reload, commandFunc))
        div {
            child(
                PlayerRoster, PlayerRosterProps(
                    players = players,
                    tribeId = tribe.id,
                    pathSetter = pathSetter,
                    className = styles["playerRoster"]
                )
            )
        }
    }
}
