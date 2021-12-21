package com.zegreatrob.coupling.client.pairassignments

import com.zegreatrob.coupling.client.child
import com.zegreatrob.coupling.client.external.react.get
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.client.player.PlayerCardProps
import com.zegreatrob.coupling.client.player.playerCard
import com.zegreatrob.coupling.client.reactFunction
import com.zegreatrob.coupling.model.pairassignmentdocument.PinnedPlayer
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.minreact.DataProps
import kotlinx.css.properties.Angle
import react.RBuilder

val RBuilder.draggablePlayer get() = { props: DraggablePlayerProps -> child(DraggablePlayer, props) }

data class DraggablePlayerProps(
    val pinnedPlayer: PinnedPlayer,
    val tribe: Tribe,
    val zoomOnHover: Boolean,
    val tilt: Angle,
    val onPlayerDrop: (String) -> Unit
) : DataProps

const val playerDragItemType = "PLAYER"

private val styles = useStyles("pairassignments/DraggablePlayer")

val DraggablePlayer = reactFunction<DraggablePlayerProps> { (pinnedPlayer, tribe, zoomOnHover, tilt, onPlayerDrop) ->
    draggableThing(playerDragItemType, pinnedPlayer.player.id, onPlayerDrop) { isOver: Boolean ->
        playerCard(
            PlayerCardProps(
                tribeId = tribe.id,
                player = pinnedPlayer.player,
                className = playerCardClassName(isOver, zoomOnHover),
                tilt = tilt
            ),
            key = pinnedPlayer.player.id
        )
    }
}

private fun playerCardClassName(isOver: Boolean, zoomOnHover: Boolean) = mapOf(
    styles["hoverZoom"] to zoomOnHover,
    styles["onDragHover"] to isOver
)
    .filterValues { it }
    .keys
    .plus(styles.className)
    .joinToString(" ")

