package com.zegreatrob.coupling.client.pairassignments

import com.zegreatrob.coupling.client.external.react.get
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.client.player.PlayerCard
import com.zegreatrob.coupling.model.pairassignmentdocument.PinnedPlayer
import com.zegreatrob.minreact.DataPropsBind
import com.zegreatrob.minreact.child
import com.zegreatrob.minreact.tmFC
import csstype.ClassName
import kotlinx.css.properties.Angle

data class DraggablePlayer(
    val pinnedPlayer: PinnedPlayer,
    val zoomOnHover: Boolean,
    val tilt: Angle,
    val onPlayerDrop: (String) -> Unit
) : DataPropsBind<DraggablePlayer> (draggablePlayer)

const val playerDragItemType = "PLAYER"

private val styles = useStyles("pairassignments/DraggablePlayer")

val draggablePlayer = tmFC<DraggablePlayer> { (pinnedPlayer, zoomOnHover, tilt, onPlayerDrop) ->
    child(DraggableThing(playerDragItemType, pinnedPlayer.player.id, onPlayerDrop) { isOver ->
        child(
            PlayerCard(
                pinnedPlayer.player,
                className = playerCardClassName(isOver, zoomOnHover),
                tilt = tilt
            ),
            key = pinnedPlayer.player.id
        )
    })
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

