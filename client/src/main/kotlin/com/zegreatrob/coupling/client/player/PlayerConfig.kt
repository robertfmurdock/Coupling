package com.zegreatrob.coupling.client.player

import com.zegreatrob.coupling.client.DispatchFunc
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.minreact.DataProps
import com.zegreatrob.minreact.child
import com.zegreatrob.minreact.tmFC

data class PlayerConfig(
    val tribe: Tribe,
    val player: Player,
    val players: List<Player>,
    val reload: () -> Unit,
    val dispatchFunc: DispatchFunc<out PlayerConfigDispatcher>
) : DataProps<PlayerConfig> {
    override val component get() = playerConfig
}

val playerConfig = tmFC { (tribe, player, players, reload, commandFunc): PlayerConfig ->
    child(PlayerConfigEditor(tribe, player, players, reload, commandFunc))
}
