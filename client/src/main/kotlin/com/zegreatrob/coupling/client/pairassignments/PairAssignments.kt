package com.zegreatrob.coupling.client.pairassignments

import PinButton
import PinButtonProps
import PinButtonScale
import com.zegreatrob.coupling.client.external.react.*
import com.zegreatrob.coupling.client.external.reactdnd.DndProvider
import com.zegreatrob.coupling.client.external.reactdndhtml5backend.HTML5Backend
import com.zegreatrob.coupling.client.pairassignments.list.dateText
import com.zegreatrob.coupling.client.player.PlayerCardProps
import com.zegreatrob.coupling.client.player.PlayerRosterProps
import com.zegreatrob.coupling.client.player.playerCard
import com.zegreatrob.coupling.client.player.playerRoster
import com.zegreatrob.coupling.client.tribe.TribeBrowserProps
import com.zegreatrob.coupling.client.tribe.tribeBrowser
import com.zegreatrob.coupling.client.user.ServerMessageProps
import com.zegreatrob.coupling.client.user.serverMessage
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pairassignmentdocument.PinnedCouplingPair
import com.zegreatrob.coupling.model.pairassignmentdocument.PinnedPlayer
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.player.callsign.CallSign
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.sdk.RepositoryCatalog
import com.zegreatrob.coupling.sdk.SdkSingleton
import kotlinx.coroutines.launch
import kotlinx.css.marginLeft
import kotlinx.css.px
import kotlinx.html.classes
import kotlinx.html.js.onClickFunction
import org.w3c.dom.events.Event
import react.RBuilder
import react.RProps
import react.ReactElement
import react.dom.a
import react.dom.div
import react.dom.key
import react.dom.span
import styled.css
import styled.styledDiv
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
    val callSign: String
    val noPairsNotice: String
    val pair: String
    val pinSection: String
    val saveButton: String
    val newPairsButton: String
    val viewHistoryButton: String
    val retiredPlayersButton: String
}

const val dragItemType = "PLAYER"

typealias PairAssignmentRenderer = ScopedStyledRContext<PairAssignmentsProps, PairAssignmentsStyles>

interface PairAssignmentsRenderer : ScopedStyledComponentRenderer<PairAssignmentsProps, PairAssignmentsStyles>,
    SavePairAssignmentsCommandDispatcher {

    override val componentPath: String get() = "pairassignments/PairAssignments"

    override fun ScopedStyledRContext<PairAssignmentsProps, PairAssignmentsStyles>.render(): ReactElement {
        val (pairAssignments, setPairAssignments) = useState(props.pairAssignments)

        val swapCallback = makeSwapCallback(pairAssignments, setPairAssignments)
        val tribe = props.tribe
        val players = props.players
        val pathSetter = props.pathSetter
        return reactElement {
            DndProvider {
                attrs { backend = HTML5Backend }
                div(classes = styles.className) {
                    div {
                        tribeBrowser(TribeBrowserProps(tribe, pathSetter))
                        child(currentPairAssignmentsElement(pairAssignments, swapCallback))
                    }
                    unpairedPlayerSection(tribe, notPairedPlayers(players, pairAssignments), pathSetter)
                    serverMessage(ServerMessageProps(tribe.id, "https:" == window.location.protocol))
                }
            }
        }
    }

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
        swapCallback: (String, PinnedPlayer, PinnedCouplingPair) -> Unit
    ) = reactElement {
        div(classes = styles.pairAssignments) {
            pairAssignmentsHeader(pairAssignments, styles)
            child(pairAssignmentListingElement(pairAssignments, swapCallback))
            child(saveButtonSectionElement(pairAssignments))

            prepareToSpinButton(props.tribe, styles.newPairsButton)
            viewHistoryButton(props.tribe, styles.viewHistoryButton)
            viewRetireesButton(props.tribe, styles.retiredPlayersButton)
        }
    }

    private fun PairAssignmentRenderer.pairAssignmentListingElement(
        pairAssignments: PairAssignmentDocument?,
        swapCallback: (String, PinnedPlayer, PinnedCouplingPair) -> Unit
    ) = reactElement {
        div(classes = styles.pairAssignmentsContent) {
            pairAssignments?.pairs?.mapIndexed { index, pair ->
                assignedPair(
                    props.tribe,
                    index,
                    pair,
                    swapCallback,
                    pairAssignments,
                    styles,
                    props.pathSetter
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
                    targetPair -> replacePlayer(pair, targetPlayer, droppedPlayer)
                    sourcePair -> replacePlayer(pair, droppedPlayer, targetPlayer)
                    else -> pair
                }
            }
        )
    }

    private fun replacePlayer(pair: PinnedCouplingPair, playerToReplace: PinnedPlayer, replacement: PinnedPlayer) =
        PinnedCouplingPair(pair.players.map { pinnedPlayer ->
            if (pinnedPlayer == playerToReplace) {
                replacement
            } else {
                pinnedPlayer
            }
        })

    private fun List<PinnedCouplingPair>.findPairContainingPlayer(droppedPlayerId: String) = firstOrNull { pair ->
        pair.players.any { player -> player.player.id == droppedPlayerId }
    }

    private fun RBuilder.assignedPair(
        tribe: Tribe,
        index: Int,
        pair: PinnedCouplingPair,
        swapCallback: (String, PinnedPlayer, PinnedCouplingPair) -> Unit,
        pairAssignmentDocument: PairAssignmentDocument?,
        styles: PairAssignmentsStyles,
        pathSetter: (String) -> Unit
    ) {
        val callSign = findCallSign(pair)

        span(classes = styles.pair) {
            attrs { key = "$index" }
            callSign(tribe, callSign, styles)
            pair.players.map { pinnedPlayer ->
                pairedPlayerCard(tribe, pinnedPlayer, pair, pairAssignmentDocument, swapCallback, pathSetter)
            }
            pinSection(styles, pair)
        }
    }

    private fun RBuilder.pinSection(styles: PairAssignmentsStyles, pair: PinnedCouplingPair) = styledDiv {
        attrs {
            classes += styles.pinSection
            css {
                marginLeft = -(pair.pins.size * 12).px
            }
        }
        pair.pins.map { pin ->
            child(PinButton(PinButtonProps(pin, PinButtonScale.Small)))
        }
    }

    private fun RBuilder.pairedPlayerCard(
        tribe: Tribe,
        pinnedPlayer: PinnedPlayer,
        pair: PinnedCouplingPair,
        pairAssignmentDocument: PairAssignmentDocument?,
        swapCallback: (String, PinnedPlayer, PinnedCouplingPair) -> Unit,
        pathSetter: (String) -> Unit
    ) = if (pairAssignmentDocument != null && pairAssignmentDocument.id == null) {
        swappablePlayer(tribe, pinnedPlayer, pair, pairAssignmentDocument, swapCallback)
    } else {
        notSwappablePlayer(tribe, pinnedPlayer, pathSetter)
    }

    private fun RBuilder.notSwappablePlayer(tribe: Tribe, pinnedPlayer: PinnedPlayer, pathSetter: (String) -> Unit) =
        playerCard(
            PlayerCardProps(
                tribe.id,
                pinnedPlayer.player,
                pathSetter,
                false
            ), key = pinnedPlayer.player.id
        )

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

    private fun RBuilder.callSign(tribe: Tribe, callSign: CallSign?, pairAssignmentsStyles: PairAssignmentsStyles) =
        div {
            if (tribe.callSignsEnabled && callSign != null) {
                span(classes = pairAssignmentsStyles.callSign) {
                    +"${callSign.adjective} ${callSign.noun}"
                }
            }
        }

    private fun findCallSign(pair: PinnedCouplingPair): CallSign? {
        val nounPlayer = pair.toPair().asArray().getOrNull(0)
        val adjectivePlayer = pair.toPair().asArray().getOrNull(1) ?: nounPlayer

        val adjective = adjectivePlayer?.callSignAdjective
        val noun = nounPlayer?.callSignNoun
        return if (adjective != null && noun != null) {
            CallSign(adjective, noun)
        } else {
            null
        }
    }

    private fun RBuilder.pairAssignmentsHeader(
        pairAssignments: PairAssignmentDocument?,
        styles: PairAssignmentsStyles
    ) {
        if (pairAssignments != null) {
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
    }

}
