package com.zegreatrob.coupling.client.components.pairassignments

import com.zegreatrob.coupling.client.components.CouplingImages
import com.zegreatrob.coupling.client.components.external.reactdnd.useDrop
import com.zegreatrob.coupling.client.components.external.reactfliptoolkit.Flipped
import com.zegreatrob.coupling.client.components.pin.PIN_DRAG_ITEM_TYPE
import com.zegreatrob.coupling.client.components.pin.PinSection
import com.zegreatrob.coupling.client.components.player.PlayerCard
import com.zegreatrob.coupling.client.components.player.create
import com.zegreatrob.coupling.model.pairassignmentdocument.PinnedCouplingPair
import com.zegreatrob.coupling.model.pairassignmentdocument.PinnedPlayer
import com.zegreatrob.coupling.model.pairassignmentdocument.callSign
import com.zegreatrob.coupling.model.pairassignmentdocument.pairId
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.model.pin.PinId
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.player.PlayerId
import com.zegreatrob.coupling.model.player.callsign.CallSign
import com.zegreatrob.minreact.ReactFunc
import com.zegreatrob.minreact.nfc
import emotion.react.css
import react.ChildrenBuilder
import react.Fragment
import react.Props
import react.ReactNode
import react.dom.html.ReactHTML
import react.dom.html.ReactHTML.div
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
import kotlin.js.Json
import kotlin.js.json

typealias PinMoveCallback = (PinId, String) -> Unit

val tiltLeft = (-8).deg
val tiltRight = 8.deg

external interface AssignedPairProps : Props {
    var party: PartyDetails
    var pair: PinnedCouplingPair
    var canDrag: Boolean
    var swapPlayersFunc: ((PinnedPlayer, PlayerId) -> Unit)?
    var pinDropFunc: PinMoveCallback?
}

@ReactFunc
val AssignedPair by nfc<AssignedPairProps> { (party, pair, canDrag, swapCallback, pinMoveCallback) ->
    val callSign = pair.callSign()

    val (pinIsOver, drop) = useDrop(
        acceptItemType = PIN_DRAG_ITEM_TYPE,
        drop = { json("dropTargetId" to pair.toPair().pairId) },
        collect = { monitor -> monitor.isOver() },
    )
    val pinDroppableRef = useRef<HTMLElement>(null)
    drop(pinDroppableRef)
    val onPinDropEnd: ((PinId, Json?) -> Unit)? = { pinId: PinId, data: Json? ->
        pinMoveCallback?.invoke(pinId, data?.get("dropTargetId").toString()) ?: Unit
    }.takeIf { canDrag }

    val playerCard = playerCardComponent(canDrag, swapCallback)

    DroppableThing(itemType = PLAYER_DRAG_ITEM_TYPE, dropCallback = { }) {
        ReactHTML.span {
            asDynamic()["data-assigned-pair"] = pair.toPair().pairId
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
                    backgroundColor = if (pinIsOver) {
                        Color("#cff8ff")
                    } else {
                        NamedColor.aliceblue
                    }
                    flexGrow = number(1.0)
                }
            }
            div {
                if (party.callSignsEnabled) {
                    callSign(callSign)
                }
            }
            div {
                pair.pinnedPlayers.toList().forEachIndexed { index, player ->
                    Fragment {
                        key = player.player.id.value.toString()
                        playerCard(player, if (index % 2 == 0) tiltLeft else tiltRight)
                    }
                }
            }

            PinSection(pinList = pair.pins.toList(), endCallback = onPinDropEnd)
        }
    }
}

private fun ChildrenBuilder.callSign(callSign: CallSign) {
    ReactHTML.span {
        asDynamic()["data-call-sign"] = ""
        css {
            position = Position.relative
            fontSize = FontSize.large
            padding = 8.px
            backgroundColor = Color("#c9d6bab8")
            backgroundImage = url(CouplingImages.images.overlayPng)
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

private fun playerCardComponent(
    canDrag: Boolean,
    swap: ((PinnedPlayer, PlayerId) -> Unit)?,
): ChildrenBuilder.(PinnedPlayer, Angle) -> Unit = if (canDrag) {
    { player, tilt ->
        playerFlipped(player.player) {
            DraggablePlayer.create(
                pinnedPlayer = player,
                zoomOnHover = canDrag,
                tilt = tilt,
                onPlayerDrop = { droppedPlayerId -> swap?.invoke(player, droppedPlayerId) },
            )
        }
    }
} else {
    { player, tilt ->
        playerFlipped(player.player) { notSwappablePlayer(player.player, tilt) }
    }
}

private fun ChildrenBuilder.playerFlipped(player: Player, handler: () -> ReactNode) = Flipped {
    flipId = player.id.value.toString()
    this.key = player.id.value.toString()
    div {
        css {
            display = Display.inlineBlock
            if (player.id.value.toString().contains("?")) {
                visibility = Visibility.hidden
            }
        }
        +handler()
    }
}

private fun notSwappablePlayer(player: Player, tilt: Angle) = PlayerCard.create(player, tilt = tilt)
