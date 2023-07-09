package com.zegreatrob.coupling.client.components.pairassignments

import com.zegreatrob.coupling.client.components.external.reactdnd.useDrop
import com.zegreatrob.coupling.client.components.external.reactfliptoolkit.Flipped
import com.zegreatrob.coupling.client.components.pairassignments.spin.placeholderPlayer
import com.zegreatrob.coupling.client.components.pin.PinSection
import com.zegreatrob.coupling.client.components.pin.pinDragItemType
import com.zegreatrob.coupling.client.components.player.PlayerCard
import com.zegreatrob.coupling.client.components.player.create
import com.zegreatrob.coupling.client.components.pngPath
import com.zegreatrob.coupling.model.pairassignmentdocument.PinnedCouplingPair
import com.zegreatrob.coupling.model.pairassignmentdocument.PinnedPlayer
import com.zegreatrob.coupling.model.pairassignmentdocument.callSign
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.player.callsign.CallSign
import com.zegreatrob.minreact.ReactFunc
import com.zegreatrob.minreact.nfc
import emotion.react.css
import react.ChildrenBuilder
import react.Props
import react.ReactNode
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.span
import react.useRef
import web.cssom.Angle
import web.cssom.BackgroundRepeat
import web.cssom.Color
import web.cssom.Display
import web.cssom.FlexDirection
import web.cssom.FontSize
import web.cssom.FontWeight
import web.cssom.LineStyle
import web.cssom.Margin
import web.cssom.NamedColor
import web.cssom.Position
import web.cssom.Visibility
import web.cssom.deg
import web.cssom.em
import web.cssom.integer
import web.cssom.number
import web.cssom.px
import web.cssom.rotatex
import web.cssom.url
import web.html.HTMLElement

typealias PinMoveCallback = (String) -> Unit

val tiltLeft = (-8).deg
val tiltRight = 8.deg

external interface AssignedPairProps : Props {
    var party: PartyDetails
    var pair: PinnedCouplingPair
    var canDrag: Boolean
    var swapPlayersFunc: ((PinnedPlayer, String) -> Unit)?
    var pinDropFunc: PinMoveCallback?
}

@ReactFunc
val AssignedPair by nfc<AssignedPairProps> { (party, pair, canDrag, swapCallback, pinMoveCallback) ->
    val callSign = pair.callSign()

    val (isOver, drop) = usePinDrop(pinMoveCallback)
    val pinDroppableRef = useRef<HTMLElement>(null)
    drop(pinDroppableRef)

    val playerCard = playerCardComponent(canDrag, swapCallback)

    span {
        asDynamic()["data-assigned-pair"] = pair.toPair().asArray().joinToString("-") { it.id }
        css {
            padding = 5.px
            display = Display.inlineFlex
            margin = Margin(0.px, 2.px, 0.px, 2.px)
            position = Position.relative
            perspective = 10.em
            flexDirection = FlexDirection.column
        }
        ref = pinDroppableRef

        div {
            css {
                position = Position.absolute
                top = 0.px
                left = 0.px
                right = 0.px
                bottom = 0.px
                transform = rotatex(15.deg)
                borderWidth = 3.px
                borderRadius = 40.px
                borderStyle = LineStyle.hidden
                borderColor = NamedColor.dimgray
                margin = 10.px
                backgroundColor = if (isOver) {
                    Color("#cff8ff")
                } else {
                    NamedColor.aliceblue
                }
                flexGrow = number(1.0)
            }
        }
        div {
            if (party.callSignsEnabled && callSign != null) {
                callSign(callSign)
            }
        }
        div {
            pair.pinnedPlayers.mapIndexed { index, player ->
                playerCard(player, if (index % 2 == 0) tiltLeft else tiltRight)
            }
        }

        PinSection(pinList = pair.pins.toList(), canDrag = canDrag)
    }
}

private fun ChildrenBuilder.callSign(callSign: CallSign) {
    span {
        asDynamic()["data-call-sign"] = ""
        css {
            position = Position.relative
            fontSize = FontSize.large
            padding = 8.px
            backgroundColor = Color("#c9d6bab8")
            backgroundImage = url(pngPath("overlay"))
            backgroundRepeat = BackgroundRepeat.repeatX
            borderRadius = 15.px
            borderWidth = 1.px
            borderStyle = LineStyle.dotted
            borderColor = NamedColor.black
            fontWeight = FontWeight.bold
            zIndex = integer(10)
        }
        +"${callSign.adjective} ${callSign.noun}"
    }
}

private fun usePinDrop(pinMoveCallback: PinMoveCallback?) = useDrop(
    acceptItemType = pinDragItemType,
    drop = { item -> pinMoveCallback?.invoke(item["id"].unsafeCast<String>()) },
    collect = { monitor -> monitor.isOver() },
)

private fun playerCardComponent(
    canDrag: Boolean,
    swap: ((PinnedPlayer, String) -> Unit)?,
): ChildrenBuilder.(PinnedPlayer, Angle) -> Unit = if (canDrag) {
    { player, tilt ->
        playerFlipped(player.player) {
            DraggablePlayer.create(
                pinnedPlayer = player,
                zoomOnHover = canDrag,
                tilt = tilt,
                onPlayerDrop = { droppedPlayerId: String -> swap?.invoke(player, droppedPlayerId) },
            )
        }
    }
} else {
    { player, tilt ->
        playerFlipped(player.player) { notSwappablePlayer(player.player, tilt) }
    }
}

private fun ChildrenBuilder.playerFlipped(player: Player, handler: () -> ReactNode) = Flipped {
    flipId = player.id
    div {
        css {
            display = Display.inlineBlock
            if (player == placeholderPlayer) {
                visibility = Visibility.hidden
            }
        }
        this.key = player.id
        +handler()
    }
}

private fun notSwappablePlayer(player: Player, tilt: Angle) = PlayerCard.create(player, tilt = tilt)
