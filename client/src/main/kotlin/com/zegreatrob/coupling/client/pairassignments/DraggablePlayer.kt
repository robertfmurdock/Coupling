package com.zegreatrob.coupling.client.pairassignments

import com.zegreatrob.coupling.client.player.PlayerCard
import com.zegreatrob.coupling.model.pairassignmentdocument.PinnedPlayer
import com.zegreatrob.minreact.DataPropsBind
import com.zegreatrob.minreact.add
import com.zegreatrob.minreact.tmFC
import csstype.AnimationIterationCount
import csstype.NamedColor
import csstype.TransitionProperty
import csstype.TransitionTimingFunction
import csstype.deg
import csstype.ident
import csstype.rotate
import csstype.s
import react.key

data class DraggablePlayer(
    val pinnedPlayer: PinnedPlayer,
    val zoomOnHover: Boolean,
    val tilt: csstype.Angle,
    val onPlayerDrop: (String) -> Unit
) : DataPropsBind<DraggablePlayer>(draggablePlayer)

const val playerDragItemType = "PLAYER"

val draggablePlayer = tmFC<DraggablePlayer> { (pinnedPlayer, zoomOnHover, tilt, onPlayerDrop) ->
    add(
        DraggableThing(
            itemType = playerDragItemType,
            itemId = pinnedPlayer.player.id,
            dropCallback = onPlayerDrop
        ) { isOver ->
            add(
                PlayerCard(
                    player = pinnedPlayer.player,
                    tilt = tilt,
                    className = emotion.css.ClassName {
                        if (zoomOnHover) {
                            hover {
                                transitionProperty = TransitionProperty.all
                                transitionTimingFunction = TransitionTimingFunction.easeIn
                                transitionDuration = 0.2.s
                                transform = rotate(0.deg)
                                animationDuration = 0.5.s
                                animationName = ident("twitch")
                                animationDelay = 0.2.s
                                animationIterationCount = AnimationIterationCount.infinite
                            }
                        }

                        if (isOver) {
                            backgroundColor = NamedColor.orange
                            animationDuration = 0.25.s
                            animationName = ident("wiggle")
                            animationIterationCount = AnimationIterationCount.infinite
                        }
                    }
                )
            ) {
                key = pinnedPlayer.player.id
            }
        }
    )
}
