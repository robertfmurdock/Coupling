package com.zegreatrob.coupling.client.components.pairassignments

import com.zegreatrob.coupling.client.components.PlayerCard
import com.zegreatrob.coupling.client.components.external.reactdnd.useDrop
import com.zegreatrob.coupling.client.components.external.reactfliptoolkit.Flipped
import com.zegreatrob.coupling.client.components.pairassignments.spin.placeholderPlayer
import com.zegreatrob.coupling.client.components.pin.PinSection
import com.zegreatrob.coupling.client.components.pin.pinDragItemType
import com.zegreatrob.coupling.client.components.pngPath
import com.zegreatrob.coupling.model.pairassignmentdocument.PinnedCouplingPair
import com.zegreatrob.coupling.model.pairassignmentdocument.PinnedPlayer
import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.player.callsign.CallSign
import com.zegreatrob.minreact.DataPropsBind
import com.zegreatrob.minreact.add
import com.zegreatrob.minreact.create
import com.zegreatrob.minreact.ntmFC
import csstype.BackgroundRepeat
import csstype.Color
import csstype.Display
import csstype.FontSize
import csstype.FontWeight
import csstype.LineStyle
import csstype.Margin
import csstype.NamedColor
import csstype.Position
import csstype.Visibility
import csstype.deg
import csstype.integer
import csstype.px
import csstype.url
import emotion.react.css
import react.ChildrenBuilder
import react.ReactNode
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.span
import react.useRef
import web.html.HTMLElement

data class AssignedPair(
    val party: Party,
    val pair: PinnedCouplingPair,
    val canDrag: Boolean,
    val swapPlayersFunc: (PinnedPlayer, String) -> Unit = { _, _ -> },
    val pinDropFunc: PinMoveCallback = {},
) : DataPropsBind<AssignedPair>(assignedPair)

typealias PinMoveCallback = (String) -> Unit

val tiltLeft = (-8).deg
val tiltRight = 8.deg

val assignedPair by ntmFC<AssignedPair> { (party, pair, canDrag, swapCallback, pinMoveCallback) ->
    val callSign = pair.findCallSign()

    val (isOver, drop) = usePinDrop(pinMoveCallback)
    val pinDroppableRef = useRef<HTMLElement>(null)
    drop(pinDroppableRef)

    val playerCard = playerCardComponent(canDrag, swapCallback)

    span {
        asDynamic()["data-assigned-pair"] = pair.toPair().asArray().joinToString("-") { it.id }
        css {
            borderWidth = 3.px
            borderStyle = LineStyle.double
            borderColor = NamedColor.dimgray
            padding = 5.px
            display = Display.inlineBlock
            borderRadius = 40.px
            margin = Margin(0.px, 2.px, 0.px, 2.px)
            position = Position.relative
            backgroundColor = if (isOver) {
                Color("#cff8ff")
            } else {
                NamedColor.aliceblue
            }
        }
        ref = pinDroppableRef
        div {
            if (party.callSignsEnabled && callSign != null) {
                callSign(callSign)
            }
        }
        pair.players.mapIndexed { index, player ->
            playerCard(player, if (index % 2 == 0) tiltLeft else tiltRight)
        }

        add(PinSection(pinList = pair.pins.toList(), canDrag = canDrag))
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

private fun PinnedCouplingPair.findCallSign(): CallSign? {
    val nounPlayer = toPair().asArray().getOrNull(0)
    val adjectivePlayer = toPair().asArray().getOrNull(1) ?: nounPlayer

    val adjective = adjectivePlayer?.callSignAdjective
    val noun = nounPlayer?.callSignNoun
    return if (adjective != null && noun != null) {
        CallSign(adjective, noun)
    } else {
        null
    }
}

private fun usePinDrop(pinMoveCallback: PinMoveCallback) = useDrop(
    acceptItemType = pinDragItemType,
    drop = { item -> pinMoveCallback(item["id"].unsafeCast<String>()) },
    collect = { monitor -> monitor.isOver() },
)

private fun playerCardComponent(
    canDrag: Boolean,
    swap: (PinnedPlayer, String) -> Unit,
): ChildrenBuilder.(PinnedPlayer, csstype.Angle) -> Unit = if (canDrag) {
    { player, tilt ->
        playerFlipped(player.player) {
            swappablePlayer(player, canDrag, tilt) { droppedPlayerId: String -> swap(player, droppedPlayerId) }
                .create()
        }
    }
} else {
    { player, tilt ->
        playerFlipped(player.player) {
            notSwappablePlayer(player.player, tilt)
                .create()
        }
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

private fun notSwappablePlayer(player: Player, tilt: csstype.Angle) = PlayerCard(player, tilt = tilt)

private fun swappablePlayer(
    pinnedPlayer: PinnedPlayer,
    zoomOnHover: Boolean,
    tilt: csstype.Angle,
    onDropSwap: (String) -> Unit,
) = DraggablePlayer(pinnedPlayer, zoomOnHover, tilt, onDropSwap)
