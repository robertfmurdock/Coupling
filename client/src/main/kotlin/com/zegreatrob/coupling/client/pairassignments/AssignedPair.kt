package com.zegreatrob.coupling.client.pairassignments

import com.zegreatrob.coupling.client.external.react.*
import com.zegreatrob.coupling.client.external.reactdnd.useDrop
import com.zegreatrob.coupling.client.pin.PinSection.pinSection
import com.zegreatrob.coupling.client.pin.pinDragItemType
import com.zegreatrob.coupling.client.player.PlayerCardProps
import com.zegreatrob.coupling.client.player.playerCard
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pairassignmentdocument.PinnedCouplingPair
import com.zegreatrob.coupling.model.pairassignmentdocument.PinnedPlayer
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.player.callsign.CallSign
import com.zegreatrob.coupling.model.tribe.Tribe
import kotlinx.html.classes
import org.w3c.dom.Node
import react.RBuilder
import react.RProps
import react.ReactElement
import react.dom.div
import react.dom.span
import kotlin.js.Json

data class AssignedPairProps(
    val tribe: Tribe,
    val pair: PinnedCouplingPair,
    val swapCallback: (String, PinnedPlayer, PinnedCouplingPair) -> Unit,
    val pinMoveCallback: (Pin, PinnedCouplingPair) -> Unit,
    val pairAssignmentDocument: PairAssignmentDocument?,
    val pathSetter: (String) -> Unit
) : RProps

object AssignedPair : FRComponent<AssignedPairProps>(provider()) {

    fun RBuilder.assignedPair(
        tribe: Tribe,
        pair: PinnedCouplingPair,
        swapCallback: (String, PinnedPlayer, PinnedCouplingPair) -> Unit,
        pinMoveCallback: (Pin, PinnedCouplingPair) -> Unit,
        pairAssignmentDocument: PairAssignmentDocument?,
        pathSetter: (String) -> Unit,
        key: String
    ) {
        child(
            AssignedPair.component.rFunction,
            AssignedPairProps(
                tribe,
                pair,
                swapCallback,
                pinMoveCallback,
                pairAssignmentDocument,
                pathSetter
            ),
            key = key
        )
    }


    override fun render(props: AssignedPairProps) = with(props) {
        val styles = useStyles("pairassignments/AssignedPair")

        val callSign = pair.findCallSign()
        val canDrag = pairAssignmentDocument.isSaved()

        val (isOver, drop) = usePinDrop()
        val pinDroppableRef = useRef<Node>(null)
        drop(pinDroppableRef)

        val playerCard = playerCardComponent(canDrag)

        reactElement {
            span(classes = styles.className) {
                attrs {
                    ref = pinDroppableRef
                    if (isOver) classes += styles["pairPinOver"]
                }
                callSign(tribe, callSign, styles["callSign"])
                pair.players.map { player -> playerCard(player) }
                pinSection(pinList = pair.pins, canDrag = canDrag)
            }
        }
    }

    private fun PairAssignmentDocument?.isSaved() = this != null && id == null

    private fun AssignedPairProps.usePinDrop() = useDrop(
        acceptItemType = pinDragItemType,
        drop = { item -> findDroppedPin(item)?.let { pinMoveCallback(it, pair) } },
        collect = { monitor -> monitor.isOver() }
    )

    private fun AssignedPairProps.playerCardComponent(canDrag: Boolean): RBuilder.(PinnedPlayer) -> ReactElement =
        if (canDrag) { player ->
            swappablePlayer(
                tribe,
                player,
                pair,
                pairAssignmentDocument!!,
                swapCallback
            )
        } else { player ->
            notSwappablePlayer(tribe, player, pathSetter)
        }

    private fun AssignedPairProps.findDroppedPin(item: Json) = pairAssignmentDocument
        ?.pairs
        ?.map(PinnedCouplingPair::pins)
        ?.flatten()
        ?.find { it._id == item["id"].unsafeCast<String>() }

    private fun RBuilder.callSign(tribe: Tribe, callSign: CallSign?, classes: String) = div {
        if (tribe.callSignsEnabled && callSign != null) {
            span(classes = classes) {
                +"${callSign.adjective} ${callSign.noun}"
            }
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

    private fun RBuilder.swappablePlayer(
        tribe: Tribe,
        pinnedPlayer: PinnedPlayer,
        pair: PinnedCouplingPair,
        pairAssignmentDocument: PairAssignmentDocument,
        swapCallback: (String, PinnedPlayer, PinnedCouplingPair) -> Unit
    ) = draggablePlayer(DraggablePlayerProps(
        pinnedPlayer,
        tribe,
        pairAssignmentDocument
    ) { droppedPlayerId -> swapCallback(droppedPlayerId, pinnedPlayer, pair) })

    private fun RBuilder.notSwappablePlayer(tribe: Tribe, pinnedPlayer: PinnedPlayer, pathSetter: (String) -> Unit) =
        playerCard(
            PlayerCardProps(
                tribe.id,
                pinnedPlayer.player,
                pathSetter,
                false
            ), key = pinnedPlayer.player.id
        )

}
