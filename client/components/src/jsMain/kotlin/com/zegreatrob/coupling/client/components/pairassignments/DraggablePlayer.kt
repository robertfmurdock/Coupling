package com.zegreatrob.coupling.client.components.pairassignments

import com.zegreatrob.coupling.client.components.player.PlayerCard
import com.zegreatrob.coupling.model.pairassignmentdocument.PinnedPlayer
import com.zegreatrob.coupling.model.player.PlayerId
import com.zegreatrob.minreact.ReactFunc
import com.zegreatrob.minreact.nfc
import emotion.css.ClassName
import kotools.types.text.toNotBlankString
import react.Props
import web.cssom.Angle
import web.cssom.AnimationIterationCount
import web.cssom.NamedColor
import web.cssom.TransitionProperty
import web.cssom.TransitionTimingFunction
import web.cssom.deg
import web.cssom.ident
import web.cssom.rotate
import web.cssom.s

const val PLAYER_DRAG_ITEM_TYPE = "PLAYER"

external interface DraggablePlayerProps : Props {
    var pinnedPlayer: PinnedPlayer
    var zoomOnHover: Boolean
    var tilt: Angle
    var onPlayerDrop: (PlayerId) -> Unit
}

@ReactFunc
val DraggablePlayer by nfc<DraggablePlayerProps> { props ->
    val (pinnedPlayer, zoomOnHover, tilt, onPlayerDrop) = props
    DraggableThing(
        itemType = PLAYER_DRAG_ITEM_TYPE,
        itemId = pinnedPlayer.player.id.value.toString(),
        dropCallback = {},
        css = {},
        endCallback = { _, data ->
            val playerId: PlayerId? =
                data?.get("dropTargetId")?.toString()
                    ?.toNotBlankString()
                    ?.getOrNull()
                    ?.let { PlayerId(it) }
            playerId?.let(onPlayerDrop)
        },
    ) { isOver: Boolean ->
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
            key = pinnedPlayer.player.id.value.toString(),
        )
    }
}
