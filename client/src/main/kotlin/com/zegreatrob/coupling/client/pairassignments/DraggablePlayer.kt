package com.zegreatrob.coupling.client.pairassignments

import com.zegreatrob.coupling.client.external.react.get
import com.zegreatrob.minreact.reactFunction
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.client.player.PlayerCardProps
import com.zegreatrob.coupling.client.player.playerCard
import com.zegreatrob.coupling.model.pairassignmentdocument.PinnedPlayer
import com.zegreatrob.coupling.model.tribe.Tribe
import react.RBuilder
import react.RProps

val RBuilder.draggablePlayer get() = { props: DraggablePlayerProps -> child(DraggablePlayer, props) {} }

data class DraggablePlayerProps(
    val pinnedPlayer: PinnedPlayer,
    val tribe: Tribe,
    val zoomOnHover: Boolean,
    val onPlayerDrop: (String) -> Unit
) : RProps

const val playerDragItemType = "PLAYER"

private val styles = useStyles("pairassignments/DraggablePlayer")

val DraggablePlayer =
    reactFunction<DraggablePlayerProps> { (pinnedPlayer, tribe, zoomOnHover, onPlayerDrop) ->
        draggableThing(playerDragItemType, pinnedPlayer.player.id!!, onPlayerDrop) { isOver: Boolean ->
            playerCard(
                PlayerCardProps(
                    tribeId = tribe.id,
                    player = pinnedPlayer.player,
                    className = playerCardClassName(isOver, zoomOnHover)
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

