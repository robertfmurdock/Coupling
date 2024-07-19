package com.zegreatrob.coupling.client.components

import com.zegreatrob.coupling.client.components.player.PlayerCard
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.minreact.ReactFunc
import react.FC
import react.Props
import web.cssom.deg

external interface TiltedPlayerListProps : Props {
    var playerList: Iterable<Player>
    var size: Int?
}

private const val MAX_TILT_ANGLE = 8

@ReactFunc
val TiltedPlayerList = FC<TiltedPlayerListProps> { props ->
    val incrementSize = (MAX_TILT_ANGLE * 2.0) / (props.playerList.count() - 1)
    props.playerList.forEachIndexed { index, player ->
        val tilt = incrementSize * index - MAX_TILT_ANGLE
        PlayerCard(player, size = props.size, tilt = tilt.deg, key = player.id)
    }
}
