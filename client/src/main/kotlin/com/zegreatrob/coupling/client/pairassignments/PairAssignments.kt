package com.zegreatrob.coupling.client.pairassignments

import com.zegreatrob.coupling.client.external.react.*
import com.zegreatrob.coupling.client.external.reactdnd.DndProvider
import com.zegreatrob.coupling.client.external.reactdndhtml5backend.HTML5Backend
import com.zegreatrob.coupling.client.pairassignments.AssignedPair.assignedPair
import com.zegreatrob.coupling.client.pairassignments.list.dateText
import com.zegreatrob.coupling.client.player.PlayerRosterProps
import com.zegreatrob.coupling.client.player.playerRoster
import com.zegreatrob.coupling.client.tribe.TribeBrowserProps
import com.zegreatrob.coupling.client.tribe.tribeBrowser
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
import kotlinx.coroutines.launch
import kotlinx.html.classes
import kotlinx.html.js.onClickFunction
import org.w3c.dom.events.Event
import react.RBuilder
import react.RProps
import react.dom.a
import react.dom.div
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
    val pairAssignmentsHeader: String
    val pairAssignmentsContent: String
    val noPairsNotice: String
    val pair: String
    val saveButton: String
    val newPairsButton: String
    val viewHistoryButton: String
    val retiredPlayersButton: String
}

typealias PairAssignmentRenderer = ScopedStyledRContext<PairAssignmentsProps, PairAssignmentsStyles>

interface PairAssignmentsRenderer : ScopedStyledComponentRenderer<PairAssignmentsProps, PairAssignmentsStyles>,
    SavePairAssignmentsCommandDispatcher {

    override val componentPath: String get() = "pairassignments/PairAssignments"

    override fun ScopedStyledRContext<PairAssignmentsProps, PairAssignmentsStyles>.render() = with(props) {
        val (pairAssignments, setPairAssignments) = useState(pairAssignments)

        val swapCallback = makeSwapCallback(pairAssignments, setPairAssignments)
        val pinDropCallback = makePinCallback(pairAssignments, setPairAssignments)
        reactElement {
            DndProvider {
                attrs { backend = HTML5Backend }
                div(classes = styles.className) {
                    div {
                        tribeBrowser(TribeBrowserProps(tribe, pathSetter))
                        child(currentPairAssignmentsElement(pairAssignments, swapCallback, pinDropCallback))
                    }
                    unpairedPlayerSection(tribe, notPairedPlayers(players, pairAssignments), pathSetter)
                    serverMessage(ServerMessageProps(tribe.id, "https:" == window.location.protocol))
                }
            }
        }
    }

    private fun makePinCallback(pA: PairAssignmentDocument?, setPairAssignments: (PairAssignmentDocument?) -> Unit) = pA
        ?.let { pairAssignments -> pairAssignments.dropThePin(setPairAssignments) }
        ?: { _, _ -> }

    private fun PairAssignmentDocument.dropThePin(setPairAssignments: (PairAssignmentDocument?) -> Unit) =
        { pin: Pin, droppedPair: PinnedCouplingPair ->
            pairs.movePinTo(pin, droppedPair)
                .let { updatedPairs -> copy(pairs = updatedPairs) }
                .let { setPairAssignments(it) }
        }

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

    private fun PairAssignmentRenderer.currentPairAssignmentsElement(
        pairAssignments: PairAssignmentDocument?,
        swapCallback: (String, PinnedPlayer, PinnedCouplingPair) -> Unit,
        pinDropCallback: (Pin, PinnedCouplingPair) -> Unit
    ) = reactElement {
        div(classes = styles.pairAssignments) {
            pairAssignmentsHeader(pairAssignments, styles)
            child(pairAssignmentListyElement(pairAssignments, swapCallback, pinDropCallback))
            child(saveButtonSectionElement(pairAssignments))

            prepareToSpinButton(props.tribe, styles.newPairsButton)
            viewHistoryButton(props.tribe, styles.viewHistoryButton)
            viewRetireesButton(props.tribe, styles.retiredPlayersButton)
        }
    }

    private fun RBuilder.pairAssignmentsHeader(
        pairAssignments: PairAssignmentDocument?,
        styles: PairAssignmentsStyles
    ) = if (pairAssignments != null) {
        div {
            div {
                div(classes = styles.pairAssignmentsHeader) {
                    +"Couples for ${pairAssignments.dateText()}"
                }
            }
        }
    } else {
        div(classes = styles.noPairsNotice) {
            +"No pair assignments yet!"
        }
    }

    private fun PairAssignmentRenderer.pairAssignmentListyElement(
        pairAssignments: PairAssignmentDocument?,
        swapCallback: (String, PinnedPlayer, PinnedCouplingPair) -> Unit,
        pinDropCallback: (Pin, PinnedCouplingPair) -> Unit
    ) = reactElement {
        div(classes = styles.pairAssignmentsContent) {
            pairAssignments?.pairs?.mapIndexed { index, pair ->
                assignedPair(
                    props.tribe,
                    pair,
                    swapCallback,
                    pinDropCallback,
                    pairAssignments,
                    props.pathSetter,
                    key = "$index"
                )
            }
        }
    }

    private fun PairAssignmentRenderer.saveButtonSectionElement(pairAssignments: PairAssignmentDocument?) =
        reactElement {
            div {
                if (pairAssignments != null && pairAssignments.id == null) {
                    child(saveButtonElement(pairAssignments))
                }
            }
        }

    private fun PairAssignmentRenderer.saveButtonElement(pairAssignments: PairAssignmentDocument) = reactElement {
        a(classes = "super green button") {
            attrs {
                classes += styles.saveButton
                onClickFunction = onClickSave(pairAssignments)
            }
            +"Save!"
        }
    }

    private fun RBuilder.viewRetireesButton(tribe: Tribe, className: String) = a(
        href = "/${tribe.id.value}/players/retired",
        classes = "large yellow button"
    ) {
        attrs { classes += className }
        +"View retirees!"
    }

    private fun RBuilder.viewHistoryButton(tribe: Tribe, className: String) = a(
        href = "/${tribe.id.value}/history/",
        classes = "large blue button"
    ) {
        attrs { classes += className }
        +"View history!"
    }

    private fun RBuilder.prepareToSpinButton(tribe: Tribe, className: String) = a(
        href = "/${tribe.id.value}/prepare/",
        classes = "large pink button"
    ) {
        attrs { classes += className }
        +"Prepare to spin!"
    }

    private fun PairAssignmentRenderer.onClickSave(pairAssignments: PairAssignmentDocument): (Event) -> Unit = {
        scope.launch {
            SavePairAssignmentsCommand(props.tribe.id, pairAssignments).perform()

            props.pathSetter("/${props.tribe.id.value}/pairAssignments/current/")
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

}
