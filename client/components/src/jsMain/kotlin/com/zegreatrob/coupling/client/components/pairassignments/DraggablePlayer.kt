package com.zegreatrob.coupling.client.components.pairassignments

import com.zegreatrob.coupling.client.components.player.PlayerCard
import com.zegreatrob.coupling.model.pairassignmentdocument.PinnedPlayer
import com.zegreatrob.minreact.DataPropsBind
import com.zegreatrob.minreact.add
import com.zegreatrob.minreact.ntmFC
import emotion.css.ClassName
import web.cssom.Angle
import web.cssom.AnimationIterationCount
import web.cssom.NamedColor
import web.cssom.TransitionProperty
import web.cssom.TransitionTimingFunction
import web.cssom.deg
import web.cssom.ident
import web.cssom.rotate
import web.cssom.s

data class DraggablePlayer(
    val pinnedPlayer: PinnedPlayer,
    val zoomOnHover: Boolean,
    val tilt: Angle,
    val onPlayerDrop: (String) -> Unit,
) : DataPropsBind<DraggablePlayer>(draggablePlayer)

const val playerDragItemType = "PLAYER"

val draggablePlayer by ntmFC<DraggablePlayer> { (pinnedPlayer, zoomOnHover, tilt, onPlayerDrop) ->
    add(
        DraggableThing(
            itemType = playerDragItemType,
            itemId = pinnedPlayer.player.id,
            dropCallback = onPlayerDrop,
        ) { isOver ->
            add(
                PlayerCard(
                    player = pinnedPlayer.player,
                    tilt = tilt,
                    className = ClassName {
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
                    },
                ),
                key = pinnedPlayer.player.id,
            )
        },
    )
}
