package com.zegreatrob.coupling.client.pairassignments

import com.zegreatrob.coupling.client.external.react.*
import com.zegreatrob.coupling.client.external.reactdnd.DndProvider
import com.zegreatrob.coupling.client.external.reactdndhtml5backend.HTML5Backend
import com.zegreatrob.coupling.client.pairassignments.CurrentPairAssignmentsPanel.currentPairAssignments
import com.zegreatrob.coupling.client.pairassignments.spin.PairAssignmentsAnimator.animator
import com.zegreatrob.coupling.client.player.PlayerRosterProps
import com.zegreatrob.coupling.client.player.playerRoster
import com.zegreatrob.coupling.client.tribe.TribeBrowser.tribeBrowser
import com.zegreatrob.coupling.client.user.ServerMessageProps
import com.zegreatrob.coupling.client.user.serverMessage
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pairassignmentdocument.PinnedCouplingPair
import com.zegreatrob.coupling.model.pairassignmentdocument.PinnedPlayer
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.sdk.RepositoryCatalog
import com.zegreatrob.coupling.sdk.SdkSingleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.html.classes
import react.RBuilder
import react.RProps
import react.dom.a
import react.dom.div
import react.dom.i
import kotlin.browser.window

object PairAssignments : RComponent<PairAssignmentsProps>(provider()), PairAssignmentsRenderer,
    RepositoryCatalog by SdkSingleton

data class PairAssignmentsProps(
    val tribe: Tribe,
    val players: List<Player>,
    val pairAssignments: PairAssignmentDocument?,
    val pathSetter: (String) -> Unit
) : RProps

external interface PairAssignmentsStyles {
    val className: String
    val pairAssignments: String
    val pair: String
    val saveButton: String
    val newPairsButton: String
    val pinListButton: String
    val statisticsButton: String
    val viewHistoryButton: String
    val retiredPlayersButton: String
    val controlPanel: String
}

interface PairAssignmentsRenderer : ScopedStyledComponentRenderer<PairAssignmentsProps, PairAssignmentsStyles>,
    SavePairAssignmentsCommandDispatcher, NullTraceIdProvider {

    override val componentPath: String get() = "pairassignments/PairAssignments"

    override fun ScopedStyledRContext<PairAssignmentsProps, PairAssignmentsStyles>.render() = with(props) {
        val (pairAssignments, setPairAssignments) = useState(pairAssignments)

        val onSwap = makeSwapCallback(pairAssignments, setPairAssignments)
        val onPinDrop = makePinCallback(pairAssignments, setPairAssignments)
        val onSave = onClickSave(pairAssignments, tribe, pathSetter, scope)
        reactElement {
            DndProvider {
                attrs { backend = HTML5Backend }
                div(classes = styles.className) {
                    div {
                        tribeBrowser(tribe, pathSetter)
                        animator(tribe, players, pairAssignments, tribe.animationEnabled) {
                            currentPairAssignments(tribe, pairAssignments, onSwap, onPinDrop, onSave, pathSetter)
                        }
                    }
                    div(classes = styles.controlPanel) {
                        div {
                            prepareToSpinButton(props.tribe, styles.newPairsButton)
                        }
                        viewHistoryButton(props.tribe, styles.viewHistoryButton)
                        pinListButton(props.tribe, styles.pinListButton)
                        statisticsButton(tribe, styles.statisticsButton)
                        viewRetireesButton(props.tribe, styles.retiredPlayersButton)
                    }
                    unpairedPlayerSection(tribe, notPairedPlayers(players, pairAssignments), pathSetter)
                    serverMessage(ServerMessageProps(tribe.id, "https:" == window.location.protocol))
                }
            }
        }
    }

    private fun makePinCallback(pA: PairAssignmentDocument?, setPairAssignments: (PairAssignmentDocument?) -> Unit) =
        pA?.dropThePin(setPairAssignments)
            ?: { _, _ -> }

    private fun PairAssignmentDocument.dropThePin(setPairAssignments: (PairAssignmentDocument?) -> Unit) =
        { pinId: String, droppedPair: PinnedCouplingPair ->
            setPairAssignments(
                copy(pairs = pairs.movePinTo(findDroppedPin(pinId, this), droppedPair))
            )
        }

    private fun findDroppedPin(id: String, pairAssignments: PairAssignmentDocument) = pairAssignments
        .pairs
        .map(PinnedCouplingPair::pins)
        .flatten()
        .first { it._id == id }


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
        playerRoster(
            PlayerRosterProps(
                label = "Unpaired players",
                players = players,
                tribeId = tribe.id,
                pathSetter = pathSetter
            )
        )

    private fun makeSwapCallback(
        pairAssignments: PairAssignmentDocument?,
        setPairAssignments: (PairAssignmentDocument?) -> Unit
    ) = { droppedPlayerId: String, targetPlayer: PinnedPlayer, targetPair: PinnedCouplingPair ->
        setPairAssignments(pairAssignments?.swapPlayers(droppedPlayerId, targetPlayer, targetPair))
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
        a(href = "/${tribe.id.value}/prepare/", classes = "super pink button") {
            attrs { classes += className }
            +"Prepare to spin!"
        }

    private fun RBuilder.viewHistoryButton(tribe: Tribe, className: String) =
        a(href = "/${tribe.id.value}/history/", classes = "large green button") {
            attrs { classes += className }
            i(classes = "fa fa-history") {}
            +" History!"
        }

    private fun RBuilder.pinListButton(tribe: Tribe, className: String) =
        a(href = "/${tribe.id.value}/pins/", classes = "large white button") {
            attrs { classes += className }
            i(classes = "fa fa-peace") {}
            +" Pin Bag!"
        }

    private fun RBuilder.statisticsButton(tribe: Tribe, className: String) =
        a(href = "/${tribe.id.value}/statistics", classes = "large gray button") {
            attrs { this.classes += className }
            i(classes = "fa fa-database") {}
            +" Statistics!"
        }

    private fun RBuilder.viewRetireesButton(tribe: Tribe, className: String) = a(
        href = "/${tribe.id.value}/players/retired",
        classes = "large yellow button"
    ) {
        attrs { classes += className }
        i(classes = "fa fa-user-slash") {}
        +" Retirees!"
    }

    private inline fun onClickSave(
        pairAssignments: PairAssignmentDocument?,
        tribe: Tribe,
        crossinline pathSetter: (String) -> Unit,
        scope: CoroutineScope
    ): () -> Unit = if (pairAssignments != null) {
        {
            scope.launch {
                SavePairAssignmentsCommand(tribe.id, pairAssignments).perform()

                pathSetter("/${tribe.id.value}/pairAssignments/current/")
            }
        }
    } else {
        {}
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

}
