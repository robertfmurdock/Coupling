package com.zegreatrob.coupling.client.pairassignments

import com.zegreatrob.coupling.client.child
import com.zegreatrob.coupling.client.external.react.get
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.client.external.reactdnd.useDrop
import com.zegreatrob.coupling.client.external.reactfliptoolkit.flipped
import com.zegreatrob.coupling.client.pairassignments.spin.placeholderPlayer
import com.zegreatrob.coupling.client.pin.PinSection
import com.zegreatrob.coupling.client.pin.pinDragItemType
import com.zegreatrob.coupling.client.player.PlayerCard
import com.zegreatrob.coupling.client.reactFunction
import com.zegreatrob.coupling.model.pairassignmentdocument.PinnedCouplingPair
import com.zegreatrob.coupling.model.pairassignmentdocument.PinnedPlayer
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.player.callsign.CallSign
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.minreact.DataProps
import com.zegreatrob.minreact.TMFC
import kotlinx.css.Display
import kotlinx.css.Visibility
import kotlinx.css.display
import kotlinx.css.properties.Angle
import kotlinx.css.properties.deg
import kotlinx.css.visibility
import kotlinx.html.classes
import org.w3c.dom.Node
import react.*
import react.dom.attrs
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.span
import react.dom.key
import react.dom.span
import styled.css
import styled.styledDiv

data class AssignedPair(
    val tribe: Tribe,
    val pair: PinnedCouplingPair,
    val canDrag: Boolean,
    val swapPlayersFunc: (PinnedPlayer, String) -> Unit = { _, _ -> },
    val pinDropFunc: PinMoveCallback = {}
) : DataProps<AssignedPair> {
    override val component: TMFC<AssignedPair> get() = assignedPair
}

typealias PinMoveCallback = (String) -> Unit

private val styles = useStyles("pairassignments/AssignedPair")

val tiltLeft = (-8).deg
val tiltRight = 8.deg

val assignedPair = reactFunction<AssignedPair> { (tribe, pair, canDrag, swapCallback, pinMoveCallback) ->
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
        child(callSign(tribe, callSign, styles["callSign"]))
        pair.players.mapIndexed { index, player ->
            child(playerCard(player, if (index % 2 == 0) tiltLeft else tiltRight))
        }
        child(PinSection(pinList = pair.pins, canDrag = canDrag))
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
): (PinnedPlayer, Angle) -> ReactElement = if (canDrag) { player, tilt ->
    playerFlipped(player.player) {
        child(swappablePlayer(player, tribe, canDrag, tilt) { droppedPlayerId: String ->
            swap(player, droppedPlayerId)
        })
    }
} else { player, tilt ->
    playerFlipped(player.player) {
        child(notSwappablePlayer(tribe, player.player, tilt))
    }
}

private fun playerFlipped(player: Player, handler: RBuilder.() -> Unit) = buildElement {
    flipped(flipId = player.id) {
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
}

private fun notSwappablePlayer(tribe: Tribe, player: Player, tilt: Angle) =
    PlayerCard(tribe.id, player, true, tilt = tilt)

private fun swappablePlayer(
    pinnedPlayer: PinnedPlayer, tribe: Tribe, zoomOnHover: Boolean, tilt: Angle, onDropSwap: (String) -> Unit
) = DraggablePlayer(pinnedPlayer, tribe, zoomOnHover, tilt, onDropSwap)

private fun callSign(tribe: Tribe, callSign: CallSign?, classes: String) = div.create {
    if (tribe.callSignsEnabled && callSign != null) {
        span {
            className = classes
            +"${callSign.adjective} ${callSign.noun}"
        }
    }
}
