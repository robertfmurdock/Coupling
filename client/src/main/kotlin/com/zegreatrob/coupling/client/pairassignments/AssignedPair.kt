package com.zegreatrob.coupling.client.pairassignments

import com.zegreatrob.coupling.client.create
import com.zegreatrob.coupling.client.external.react.get
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.client.external.reactdnd.useDrop
import com.zegreatrob.coupling.client.external.reactfliptoolkit.Flipped
import com.zegreatrob.coupling.client.pairassignments.spin.placeholderPlayer
import com.zegreatrob.coupling.client.pin.PinSection
import com.zegreatrob.coupling.client.pin.pinDragItemType
import com.zegreatrob.coupling.client.player.PlayerCard
import com.zegreatrob.coupling.model.pairassignmentdocument.PinnedCouplingPair
import com.zegreatrob.coupling.model.pairassignmentdocument.PinnedPlayer
import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.player.callsign.CallSign
import com.zegreatrob.minreact.DataPropsBind
import com.zegreatrob.minreact.child
import com.zegreatrob.minreact.tmFC
import csstype.ClassName
import csstype.Display
import csstype.Visibility
import csstype.deg
import emotion.react.css
import org.w3c.dom.Node
import react.ChildrenBuilder
import react.ReactNode
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.span
import react.key
import react.useRef

data class AssignedPair(
    val party: Party,
    val pair: PinnedCouplingPair,
    val canDrag: Boolean,
    val swapPlayersFunc: (PinnedPlayer, String) -> Unit = { _, _ -> },
    val pinDropFunc: PinMoveCallback = {}
) : DataPropsBind<AssignedPair>(assignedPair)

typealias PinMoveCallback = (String) -> Unit

private val styles = useStyles("pairassignments/AssignedPair")

val tiltLeft = (-8).deg
val tiltRight = 8.deg

val assignedPair = tmFC<AssignedPair> { (party, pair, canDrag, swapCallback, pinMoveCallback) ->
    val callSign = pair.findCallSign()

    val (isOver, drop) = usePinDrop(pinMoveCallback)
    val pinDroppableRef = useRef<Node>(null)
    drop(pinDroppableRef)

    val playerCard = playerCardComponent(canDrag, swapCallback)

    span {
        className = styles.className
        ref = pinDroppableRef
        if (isOver) className = ClassName("$className ${styles["pairPinOver"]}")
        callSign(party, callSign, styles["callSign"])
        pair.players.mapIndexed { index, player ->
            playerCard(player, if (index % 2 == 0) tiltLeft else tiltRight)
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
    canDrag: Boolean,
    swap: (PinnedPlayer, String) -> Unit
): ChildrenBuilder.(PinnedPlayer, csstype.Angle) -> Unit = if (canDrag) { player, tilt ->
    playerFlipped(player.player) {
        swappablePlayer(player, canDrag, tilt) { droppedPlayerId: String -> swap(player, droppedPlayerId) }
            .create()
    }
} else { player, tilt ->
    playerFlipped(player.player) {
        notSwappablePlayer(player.player, tilt)
            .create()
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
    onDropSwap: (String) -> Unit
) = DraggablePlayer(pinnedPlayer, zoomOnHover, tilt, onDropSwap)

private fun ChildrenBuilder.callSign(party: Party, callSign: CallSign?, classes: ClassName) = div {
    if (party.callSignsEnabled && callSign != null) {
        span {
            className = classes
            +"${callSign.adjective} ${callSign.noun}"
        }
    }
}
