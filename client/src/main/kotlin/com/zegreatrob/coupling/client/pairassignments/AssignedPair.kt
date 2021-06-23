package com.zegreatrob.coupling.client.pairassignments

import com.zegreatrob.coupling.client.external.react.get
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.client.external.reactdnd.useDrop
import com.zegreatrob.coupling.client.external.reactfliptoolkit.flipped
import com.zegreatrob.coupling.client.pairassignments.spin.placeholderPlayer
import com.zegreatrob.coupling.client.pin.pinDragItemType
import com.zegreatrob.coupling.client.pin.pinSection
import com.zegreatrob.coupling.client.player.PlayerCardProps
import com.zegreatrob.coupling.client.player.playerCard
import com.zegreatrob.coupling.model.pairassignmentdocument.PinnedCouplingPair
import com.zegreatrob.coupling.model.pairassignmentdocument.PinnedPlayer
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.player.callsign.CallSign
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.minreact.child
import com.zegreatrob.minreact.reactFunction
import kotlinx.css.Display
import kotlinx.css.Visibility
import kotlinx.css.display
import kotlinx.css.properties.Angle
import kotlinx.css.properties.deg
import kotlinx.css.visibility
import kotlinx.html.classes
import org.w3c.dom.Node
import react.RBuilder
import react.RProps
import react.ReactElement
import react.dom.attrs
import react.dom.div
import react.dom.key
import react.dom.span
import react.useRef
import styled.css
import styled.styledDiv

data class AssignedPairProps(
    val tribe: Tribe,
    val pair: PinnedCouplingPair,
    val canDrag: Boolean,
    val swapPlayersFunc: (PinnedPlayer, String) -> Unit,
    val pinDropFunc: PinMoveCallback
) : RProps

typealias PinMoveCallback = (String) -> Unit

private val styles = useStyles("pairassignments/AssignedPair")

fun RBuilder.assignedPair(
    tribe: Tribe,
    pair: PinnedCouplingPair,
    swapPlayersFunc: (PinnedPlayer, String) -> Unit,
    dropPinFunc: PinMoveCallback,
    canDrag: Boolean,
    key: String
) = child(
    AssignedPair,
    AssignedPairProps(tribe, pair, canDrag, swapPlayersFunc, dropPinFunc),
    key = key
)

val AssignedPair = reactFunction<AssignedPairProps> { props ->
    val (tribe, pair, canDrag, swapCallback, pinMoveCallback) = props
    val callSign = pair.findCallSign()

    val (isOver, drop) = usePinDrop(pinMoveCallback)
    val pinDroppableRef = useRef<Node>(null)
    drop(pinDroppableRef)

    val playerCard = playerCardComponent(tribe, canDrag, swapCallback)

    span(classes = styles.className) {
        attrs {
            ref = pinDroppableRef
            if (isOver) classes = classes + styles["pairPinOver"]
        }
        callSign(tribe, callSign, styles["callSign"])
        pair.players.mapIndexed { index, player -> playerCard(player, if (index % 2 == 0) (-8).deg else 8.deg) }
        pinSection(pinList = pair.pins, canDrag = canDrag)
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
    collect = { monitor -> monitor.isOver() }
)

private fun playerCardComponent(
    tribe: Tribe,
    canDrag: Boolean,
    swap: (PinnedPlayer, String) -> Unit
): RBuilder.(PinnedPlayer, Angle) -> ReactElement = if (canDrag) { player, tilt ->
    playerFlipped(player.player) {
        swappablePlayer(tribe, player, canDrag, tilt) { droppedPlayerId: String ->
            swap(player, droppedPlayerId)
        }
    }
} else { player, tilt ->
    playerFlipped(player.player) {
        notSwappablePlayer(tribe, player.player, tilt)
    }
}

private fun RBuilder.playerFlipped(player: Player, handler: RBuilder.() -> ReactElement) = flipped(flipId = player.id) {
    styledDiv {
        attrs { this.key = player.id }
        css {
            display = Display.inlineBlock
            if (player == placeholderPlayer) {
                visibility = Visibility.hidden
            }
        }
        handler()
    }
}

private fun RBuilder.notSwappablePlayer(tribe: Tribe, player: Player, tilt: Angle) =
    playerCard(PlayerCardProps(tribe.id, player, true, tilt = tilt))

private fun RBuilder.swappablePlayer(
    tribe: Tribe,
    pinnedPlayer: PinnedPlayer,
    zoomOnHover: Boolean,
    tilt: Angle,
    onDropSwap: (String) -> Unit
) = draggablePlayer(DraggablePlayerProps(pinnedPlayer, tribe, zoomOnHover, tilt, onDropSwap))

private fun RBuilder.callSign(tribe: Tribe, callSign: CallSign?, classes: String) = div {
    if (tribe.callSignsEnabled && callSign != null) {
        span(classes = classes) {
            +"${callSign.adjective} ${callSign.noun}"
        }
    }
}
