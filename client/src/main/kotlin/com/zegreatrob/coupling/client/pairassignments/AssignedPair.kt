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
import react.dom.div
import react.dom.key
import react.dom.span
import react.useRef
import styled.css
import styled.styledDiv

data class AssignedPairProps(
    val tribe: Tribe,
    val pair: PinnedCouplingPair,
    val swapCallback: SwapCallback,
    val pinMoveCallback: PinMoveCallback,
    val canDrag: Boolean,
    val pathSetter: (String) -> Unit
) : RProps

typealias SwapCallback = (String, PinnedPlayer, PinnedCouplingPair) -> Unit
typealias PinMoveCallback = (String, PinnedCouplingPair) -> Unit

private val styles = useStyles("pairassignments/AssignedPair")

fun RBuilder.assignedPair(
    tribe: Tribe,
    pair: PinnedCouplingPair,
    swapCallback: SwapCallback,
    pinMoveCallback: PinMoveCallback,
    canDrag: Boolean,
    pathSetter: (String) -> Unit,
    key: String
) = child(
    AssignedPair,
    AssignedPairProps(tribe, pair, swapCallback, pinMoveCallback, canDrag, pathSetter),
    key = key
)

val AssignedPair = reactFunction<AssignedPairProps> { props ->
    val (tribe, pair, swapCallback, pinMoveCallback, canDrag, pathSetter) = props
    val callSign = pair.findCallSign()

    val (isOver, drop) = usePinDrop(pinMoveCallback, pair)
    val pinDroppableRef = useRef<Node?>(null)
    drop(pinDroppableRef)

    val playerCard = playerCardComponent(tribe, pair, swapCallback, canDrag, pathSetter)

    span(classes = styles.className) {
        attrs {
            ref = pinDroppableRef
            if (isOver) classes += styles["pairPinOver"]
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

private fun usePinDrop(pinMoveCallback: PinMoveCallback, pair: PinnedCouplingPair) = useDrop(
    acceptItemType = pinDragItemType,
    drop = { item -> pinMoveCallback(item["id"].unsafeCast<String>(), pair) },
    collect = { monitor -> monitor.isOver() }
)

private fun playerCardComponent(
    tribe: Tribe,
    pair: PinnedCouplingPair,
    swapCallback: SwapCallback,
    canDrag: Boolean,
    pathSetter: (String) -> Unit
): RBuilder.(PinnedPlayer, Angle) -> ReactElement =
    if (canDrag) { player, tilt ->
        playerFlipped(player.player) {
            swappablePlayer(tribe, player, pair, swapCallback, canDrag, tilt)
        }
    } else { player, tilt ->
        playerFlipped(player.player) {
            notSwappablePlayer(tribe, pathSetter, player.player, tilt)
        }
    }

private fun RBuilder.playerFlipped(player: Player, handler: RBuilder.() -> ReactElement) = flipped(flipId = player.id) {
    styledDiv {
        attrs { this.key = player.id ?: "" }
        css {
            display = Display.inlineBlock
            if (player == placeholderPlayer) {
                visibility = Visibility.hidden
            }
        }
        handler()
    }
}

private fun RBuilder.notSwappablePlayer(tribe: Tribe, pathSetter: (String) -> Unit, player: Player, tilt: Angle) =
    playerCard(PlayerCardProps(tribe.id, player, pathSetter, tilt = tilt))

private fun RBuilder.swappablePlayer(
    tribe: Tribe,
    pinnedPlayer: PinnedPlayer,
    pair: PinnedCouplingPair,
    swapCallback: SwapCallback,
    zoomOnHover: Boolean,
    tilt: Angle
) = draggablePlayer(DraggablePlayerProps(pinnedPlayer, tribe, zoomOnHover, tilt) { droppedPlayerId ->
    swapCallback(droppedPlayerId, pinnedPlayer, pair)
})

private fun RBuilder.callSign(tribe: Tribe, callSign: CallSign?, classes: String) = div {
    if (tribe.callSignsEnabled && callSign != null) {
        span(classes = classes) {
            +"${callSign.adjective} ${callSign.noun}"
        }
    }
}
