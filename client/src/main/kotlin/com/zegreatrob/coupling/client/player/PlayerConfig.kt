package com.zegreatrob.coupling.client.player

import com.zegreatrob.coupling.client.DispatchFunc
import com.zegreatrob.coupling.client.configFrame
import com.zegreatrob.coupling.client.external.react.get
import com.zegreatrob.coupling.client.external.react.reactFunction
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.repository.player.PlayerRepository
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

interface PlayerConfigDispatcher : SavePlayerCommandDispatcher, DeletePlayerCommandDispatcher {
    override val playerRepository: PlayerRepository
}

private val styles = useStyles("player/PlayerConfig")

val PlayerConfig = reactFunction<PlayerConfigProps> { (tribe, player, players, pathSetter, reload, commandFunc) ->
    configFrame(styles.className) {
        playerConfigEditor(tribe, player, pathSetter, reload, commandFunc)
        div {
            playerRoster(
                PlayerRosterProps(
                    players = players,
                    tribeId = tribe.id,
                    pathSetter = pathSetter,
                    className = styles["playerRoster"]
                )
            )
        }
    }
}
