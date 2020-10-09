package com.zegreatrob.coupling.client.pairassignments

import com.zegreatrob.coupling.client.Controls
import com.zegreatrob.coupling.client.dom.*
import com.zegreatrob.coupling.client.external.react.get
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.client.external.reactdnd.DndProvider
import com.zegreatrob.coupling.client.external.reactdndhtml5backend.HTML5Backend
import com.zegreatrob.coupling.client.pairassignments.list.DeletePairAssignmentsCommandDispatcher
import com.zegreatrob.coupling.client.pairassignments.spin.animator
import com.zegreatrob.coupling.client.player.PlayerRoster
import com.zegreatrob.coupling.client.player.PlayerRosterProps
import com.zegreatrob.coupling.client.tribe.tribeBrowser
import com.zegreatrob.coupling.client.user.serverMessage
import com.zegreatrob.coupling.model.CouplingSocketMessage
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.repository.pairassignmentdocument.PairAssignmentDocumentRepository
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

interface PairAssignmentsCommandDispatcher : SavePairAssignmentsCommandDispatcher,
    DeletePairAssignmentsCommandDispatcher {
    override val pairAssignmentDocumentRepository: PairAssignmentDocumentRepository
}

fun RBuilder.pairAssignments(
    tribe: Tribe,
    players: List<Player>,
    pairAssignments: PairAssignmentDocument?,
    updatePairAssignments: (PairAssignmentDocument) -> Unit,
    controls: Controls<PairAssignmentsCommandDispatcher>,
    message: CouplingSocketMessage,
    allowSave: Boolean,
) = child(
    PairAssignments,
    PairAssignmentsProps(tribe, players, pairAssignments, updatePairAssignments, controls, message, allowSave)
)

data class PairAssignmentsProps(
    val tribe: Tribe,
    val players: List<Player>,
    val pairAssignments: PairAssignmentDocument?,
    val sendUpdatedPairs: (PairAssignmentDocument) -> Unit,
    val controls: Controls<PairAssignmentsCommandDispatcher>,
    val message: CouplingSocketMessage,
    val allowSave: Boolean
) : RProps

private val styles = useStyles("pairassignments/PairAssignments")

val PairAssignments = reactFunction<PairAssignmentsProps> { props ->
    val (tribe, players, pairAssignments, setPairs, controls, message, allowSave) = props
    DndProvider {
        attrs { backend = HTML5Backend }
        div(classes = styles.className) {
            div {
                tribeBrowser(tribe, controls.pathSetter)
                currentPairSection(tribe, players, pairAssignments, allowSave, setPairs, controls)
            }
            controlPanel(tribe)
            unpairedPlayerSection(tribe, notPairedPlayers(players, pairAssignments), controls.pathSetter)

            serverMessage(tribe, message)
        }
    }
}

private fun RBuilder.currentPairSection(
    tribe: Tribe,
    players: List<Player>,
    pairAssignments: PairAssignmentDocument?,
    allowSave: Boolean,
    sendUpdatedPairs: (PairAssignmentDocument) -> Unit,
    controls: Controls<PairAssignmentsCommandDispatcher>
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
        animator(tribe, players, pairAssignments, enabled = tribe.animationEnabled && allowSave) {
            currentPairAssignments(
                tribe = tribe,
                pairAssignments = pairAssignments,
                setPairAssignments = sendUpdatedPairs,
                allowSave = allowSave,
                controls = controls
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

private fun RBuilder.unpairedPlayerSection(tribe: Tribe, players: List<Player>, pathSetter: (String) -> Unit) = child(
    PlayerRoster, PlayerRosterProps(
        label = "Unpaired players",
        players = players,
        tribeId = tribe.id,
        pathSetter = pathSetter
    )
)

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
