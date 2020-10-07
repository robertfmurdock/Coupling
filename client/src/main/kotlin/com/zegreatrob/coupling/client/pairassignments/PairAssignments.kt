package com.zegreatrob.coupling.client.pairassignments

import com.zegreatrob.coupling.client.DispatchFunc
import com.zegreatrob.coupling.client.currentPairs
import com.zegreatrob.coupling.client.dom.*
import com.zegreatrob.coupling.client.external.react.get
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.client.external.reactdnd.DndProvider
import com.zegreatrob.coupling.client.external.reactdndhtml5backend.HTML5Backend
import com.zegreatrob.coupling.client.pairassignments.spin.animator
import com.zegreatrob.coupling.client.player.PlayerRoster
import com.zegreatrob.coupling.client.player.PlayerRosterProps
import com.zegreatrob.coupling.client.tribe.tribeBrowser
import com.zegreatrob.coupling.client.user.serverMessage
import com.zegreatrob.coupling.model.CouplingSocketMessage
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pairassignmentdocument.PinnedCouplingPair
import com.zegreatrob.coupling.model.pairassignmentdocument.PinnedPlayer
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.minreact.child
import com.zegreatrob.minreact.reactFunction
import kotlinx.css.*
import kotlinx.css.properties.boxShadow
import react.RBuilder
import react.RProps
import react.dom.div
import react.dom.i
import react.router.dom.routeLink
import styled.css
import styled.styledDiv


fun RBuilder.pairAssignments(
    tribe: Tribe,
    players: List<Player>,
    pairAssignments: PairAssignmentDocument?,
    updatePairAssignments: (PairAssignmentDocument) -> Unit,
    commandFunc: DispatchFunc<out SavePairAssignmentsCommandDispatcher>,
    message: CouplingSocketMessage,
    allowSave: Boolean,
    pathSetter: (String) -> Unit
) = child(
    PairAssignments,
    PairAssignmentsProps(
        tribe,
        players,
        pairAssignments,
        updatePairAssignments,
        commandFunc,
        message,
        allowSave,
        pathSetter
    )
)

data class PairAssignmentsProps(
    val tribe: Tribe,
    val players: List<Player>,
    val pairAssignments: PairAssignmentDocument?,
    val sendUpdatedPairs: (PairAssignmentDocument) -> Unit,
    val dispatchFunc: DispatchFunc<out SavePairAssignmentsCommandDispatcher>,
    val message: CouplingSocketMessage,
    val allowSave: Boolean,
    val pathSetter: (String) -> Unit
) : RProps

private val styles = useStyles("pairassignments/PairAssignments")

val PairAssignments = reactFunction<PairAssignmentsProps> { props ->
    val (tribe, players, pairAssignments, sendUpdatedPairs, commandFunc, message, allowSave, pathSetter) = props
    DndProvider {
        attrs { backend = HTML5Backend }
        div(classes = styles.className) {
            div {
                tribeBrowser(tribe, pathSetter)
                currentPairSection(
                    tribe,
                    players,
                    pairAssignments,
                    pathSetter,
                    allowSave,
                    sendUpdatedPairs,
                    commandFunc
                )
            }
            controlPanel(tribe)
            unpairedPlayerSection(tribe, notPairedPlayers(players, pairAssignments), pathSetter)

            serverMessage(tribe, message)
        }
    }
}

private fun RBuilder.currentPairSection(
    tribe: Tribe,
    players: List<Player>,
    pairAssignments: PairAssignmentDocument?,
    pathSetter: (String) -> Unit,
    allowSave: Boolean,
    sendUpdatedPairs: (PairAssignmentDocument) -> Unit,
    commandFunc: DispatchFunc<out SavePairAssignmentsCommandDispatcher>
) = styledDiv {
    css {
        display = Display.inlineBlock
        borderRadius = 20.px
        padding(5.px)
        margin(5.px, 0.px)
        backgroundColor = hsla(146, 17, 80, 1.0)
        boxShadow(rgba(0, 0, 0, 0.6), 1.px, 1.px, 3.px)
    }
    if (pairAssignments == null) {
        noPairsHeader()
    } else {
        animator(tribe, players, pairAssignments, tribe.animationEnabled) {
            currentPairAssignments(
                tribe = tribe,
                pairAssignments = pairAssignments,
                onPlayerSwap = pairAssignments.makeSwapCallback(sendUpdatedPairs),
                onPinDrop = pairAssignments.makePinCallback(sendUpdatedPairs),
                allowSave = allowSave,
                onSave = pairAssignments.onSaveFunc(commandFunc, tribe, pathSetter),
                pathSetter = pathSetter
            )
        }
    }
}

private fun RBuilder.noPairsHeader() = styledDiv {
    css {
        border = "8px outset dimgray"
        backgroundColor = Color.aliceBlue
        display = Display.inlineBlock
        borderRadius = 40.px
        fontSize = LinearDimension("xx-large")
        fontWeight = FontWeight.bold
        width = 500.px
        height = 150.px
        padding(100.px, 5.px, 5.px)
        margin(0.px, 2.px, 5.px)
    }
    +"No pair assignments yet!"
}

private fun RBuilder.controlPanel(tribe: Tribe) = div(classes = styles["controlPanel"]) {
    div { prepareToSpinButton(tribe, styles["newPairsButton"]) }
    viewHistoryButton(tribe, styles["viewHistoryButton"])
    pinListButton(tribe, styles["pinListButton"])
    statisticsButton(tribe, styles["statisticsButton"])
    viewRetireesButton(tribe, styles["retiredPlayersButton"])
}

private fun PairAssignmentDocument.onSaveFunc(
    dispatchFunc: DispatchFunc<out SavePairAssignmentsCommandDispatcher>,
    tribe: Tribe,
    pathSetter: (String) -> Unit
) = dispatchFunc({ SavePairAssignmentsCommand(tribe.id, this) }, { pathSetter.currentPairs(tribe.id) })

private fun PairAssignmentDocument.makePinCallback(setPairAssignments: (PairAssignmentDocument) -> Unit) =
    dropThePin(setPairAssignments)

private fun PairAssignmentDocument.dropThePin(setPairAssignments: (PairAssignmentDocument) -> Unit) =
    { pinId: String, droppedPair: PinnedCouplingPair ->
        setPairAssignments(
            copy(pairs = pairs.movePinTo(findDroppedPin(pinId, this), droppedPair))
        )
    }

private fun findDroppedPin(id: String, pairAssignments: PairAssignmentDocument) = pairAssignments
    .pairs
    .map(PinnedCouplingPair::pins)
    .flatten()
    .first { it.id == id }


private fun List<PinnedCouplingPair>.movePinTo(pin: Pin, droppedPair: PinnedCouplingPair) = map { pair ->
    when {
        pair == droppedPair -> pair.addPin(pin)
        pair.pins.contains(pin) -> pair.removePin(pin)
        else -> pair
    }
}

private fun PinnedCouplingPair.addPin(pin: Pin) = copy(pins = pins + pin)

private fun PinnedCouplingPair.removePin(pin: Pin) = copy(pins = pins - pin)

private fun RBuilder.unpairedPlayerSection(tribe: Tribe, players: List<Player>, pathSetter: (String) -> Unit) =
    child(
        PlayerRoster, PlayerRosterProps(
            label = "Unpaired players",
            players = players,
            tribeId = tribe.id,
            pathSetter = pathSetter
        )
    )

private fun PairAssignmentDocument.makeSwapCallback(setPairAssignments: (PairAssignmentDocument) -> Unit) =
    { droppedPlayerId: String, targetPlayer: PinnedPlayer, targetPair: PinnedCouplingPair ->
        setPairAssignments(swapPlayers(droppedPlayerId, targetPlayer, targetPair))
    }

private fun notPairedPlayers(players: List<Player>, pairAssignments: PairAssignmentDocument?) =
    if (pairAssignments == null) {
        players
    } else {
        val currentlyPairedPlayerIds = pairAssignments.currentlyPairedPlayerIds()
        players.filterNot { player -> currentlyPairedPlayerIds.contains(player.id) }
    }

private fun PairAssignmentDocument.currentlyPairedPlayerIds() = pairs.flatMap { it.players }.map { it.player.id }

private fun RBuilder.prepareToSpinButton(tribe: Tribe, className: String) =
    routeLink(to = "/${tribe.id.value}/prepare/") {
        couplingButton(supersize, pink, className) {
            +"Prepare to spin!"
        }
    }

private fun RBuilder.viewHistoryButton(tribe: Tribe, className: String) =
    routeLink(to = "/${tribe.id.value}/history/") {
        couplingButton(large, green, className) {
            i(classes = "fa fa-history") {}
            +" History!"
        }
    }

private fun RBuilder.pinListButton(tribe: Tribe, className: String) = routeLink(to = "/${tribe.id.value}/pins/") {
    couplingButton(large, white, className) {
        i(classes = "fa fa-peace") {}
        +" Pin Bag!"
    }
}

private fun RBuilder.statisticsButton(tribe: Tribe, className: String) =
    routeLink(to = "/${tribe.id.value}/statistics") {
        couplingButton(large, className = className) {
            i(classes = "fa fa-database") {}
            +" Statistics!"
        }
    }

private fun RBuilder.viewRetireesButton(tribe: Tribe, className: String) =
    routeLink("/${tribe.id.value}/players/retired") {
        couplingButton(large, yellow, className) {
            i(classes = "fa fa-user-slash") {}
            +" Retirees!"
        }
    }

private fun PairAssignmentDocument.swapPlayers(
    droppedPlayerId: String,
    targetPlayer: PinnedPlayer,
    targetPair: PinnedCouplingPair
): PairAssignmentDocument {
    val sourcePair = pairs.findPairContainingPlayer(droppedPlayerId)
    val droppedPlayer = sourcePair?.players?.firstOrNull { it.player.id == droppedPlayerId }

    if (sourcePair == targetPair || droppedPlayer == null) {
        return this
    }

    return copy(
        pairs = pairs.map { pair ->
            when (pair) {
                targetPair -> pair.replacePlayer(targetPlayer, droppedPlayer)
                sourcePair -> pair.replacePlayer(droppedPlayer, targetPlayer)
                else -> pair
            }
        }
    )
}

private fun PinnedCouplingPair.replacePlayer(playerToReplace: PinnedPlayer, replacement: PinnedPlayer) =
    copy(players = players.map { pinnedPlayer ->
        if (pinnedPlayer == playerToReplace) {
            replacement
        } else {
            pinnedPlayer
        }
    })

private fun List<PinnedCouplingPair>.findPairContainingPlayer(droppedPlayerId: String) = firstOrNull { pair ->
    pair.players.any { player -> player.player.id == droppedPlayerId }
}
