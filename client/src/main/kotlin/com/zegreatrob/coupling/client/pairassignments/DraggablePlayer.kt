package com.zegreatrob.coupling.client.pairassignments

import com.zegreatrob.coupling.client.external.react.get
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.client.player.PlayerCard
import com.zegreatrob.coupling.model.pairassignmentdocument.PinnedPlayer
import com.zegreatrob.minreact.DataPropsBind
import com.zegreatrob.minreact.create
import com.zegreatrob.minreact.tmFC
import csstype.ClassName
import react.key

data class DraggablePlayer(
    val pinnedPlayer: PinnedPlayer,
    val zoomOnHover: Boolean,
    val tilt: csstype.Angle,
    val onPlayerDrop: (String) -> Unit
) : DataPropsBind<DraggablePlayer>(draggablePlayer)

const val playerDragItemType = "PLAYER"

private val styles = useStyles("pairassignments/DraggablePlayer")

val draggablePlayer = tmFC<DraggablePlayer> { (pinnedPlayer, zoomOnHover, tilt, onPlayerDrop) ->
    +DraggableThing(playerDragItemType, pinnedPlayer.player.id, onPlayerDrop) { isOver ->
        +PlayerCard(
            pinnedPlayer.player,
            className = playerCardClassName(isOver, zoomOnHover),
            tilt = tilt
        ).create {
            key = pinnedPlayer.player.id
        }
    }.create()
}

private fun playerCardClassName(isOver: Boolean, zoomOnHover: Boolean) = mapOf(
    styles["hoverZoom"] to zoomOnHover,
    styles["onDragHover"] to isOver
)
    .filterValues { it }
    .keys
    .plus(styles.className)
    .joinToString(" ")
    .let { ClassName(it) }
