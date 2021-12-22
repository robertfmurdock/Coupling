package com.zegreatrob.coupling.client.pairassignments

import com.zegreatrob.coupling.client.external.react.get
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.client.player.PlayerCard
import com.zegreatrob.coupling.model.pairassignmentdocument.PinnedPlayer
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.minreact.DataProps
import com.zegreatrob.minreact.TMFC
import com.zegreatrob.minreact.child
import com.zegreatrob.minreact.tmFC
import kotlinx.css.properties.Angle

data class DraggablePlayer(
    val pinnedPlayer: PinnedPlayer,
    val tribe: Tribe,
    val zoomOnHover: Boolean,
    val tilt: Angle,
    val onPlayerDrop: (String) -> Unit
) : DataProps<DraggablePlayer> {
    override val component: TMFC<DraggablePlayer> get() = draggablePlayer
}

const val playerDragItemType = "PLAYER"

private val styles = useStyles("pairassignments/DraggablePlayer")

val draggablePlayer = tmFC<DraggablePlayer> { (pinnedPlayer, tribe, zoomOnHover, tilt, onPlayerDrop) ->
    child(DraggableThing(playerDragItemType, pinnedPlayer.player.id, onPlayerDrop) { isOver ->
        child(
            PlayerCard(
                tribe.id,
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

