package com.zegreatrob.coupling.components.pairassignments

import com.zegreatrob.coupling.action.pairassignmentdocument.DeletePairAssignmentsCommand
import com.zegreatrob.coupling.components.Controls
import com.zegreatrob.coupling.components.ServerMessage
import com.zegreatrob.coupling.components.external.reactdnd.DndProvider
import com.zegreatrob.coupling.components.external.reactdndhtml5backend.html5BackendDeferred
import com.zegreatrob.coupling.components.party.PartyBrowser
import com.zegreatrob.coupling.components.player.PlayerRoster
import com.zegreatrob.coupling.model.CouplingSocketMessage
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.minreact.DataPropsBind
import com.zegreatrob.minreact.add
import com.zegreatrob.minreact.tmFC
import com.zegreatrob.react.dataloader.DataLoader
import com.zegreatrob.react.dataloader.EmptyState
import com.zegreatrob.react.dataloader.PendingState
import com.zegreatrob.react.dataloader.ResolvedState
import csstype.Color
import csstype.Display
import csstype.LineStyle
import csstype.Padding
import csstype.PropertiesBuilder
import csstype.px
import csstype.vh
import emotion.css.ClassName
import react.FC
import react.PropsWithChildren
import react.dom.html.ReactHTML.div

data class PairAssignments(
    val party: Party,
    val players: List<Player>,
    val pairs: PairAssignmentDocument?,
    val setPairs: (PairAssignmentDocument) -> Unit,
    val controls: Controls<DeletePairAssignmentsCommand.Dispatcher>,
    val message: CouplingSocketMessage,
    val allowSave: Boolean,
) : DataPropsBind<PairAssignments>(pairAssignments)

val pairAssignmentsClassName = ClassName { pairAssignmentStyles() }

private val pairAssignments = tmFC<PairAssignments> { props ->
    val (party, players, pairs, setPairs, controls, message, allowSave) = props

    val pairAssignments = pairs?.overlayUpdatedPlayers(players)
    val notPairedPlayers = notPairedPlayers(players, pairs)

    Html5DndProvider {
        div {
            println("hi")
            className = pairAssignmentsClassName
            div {
                add(PartyBrowser(party))
                add(PairSection(party, players, pairAssignments, allowSave, setPairs, controls))
            }
            add(ControlPanel(party))
            add(PlayerRoster(label = "Unpaired players", partyId = party.id, players = notPairedPlayers))
            add(ServerMessage(message), key = "${message.text} ${message.players.size}")
        }
    }
}

val Html5DndProvider = FC<PropsWithChildren> { props ->
    add(
        DataLoader({ html5BackendDeferred.await() }, { null }) { state ->
            when (state) {
                is EmptyState -> div { +"Preparing component" }
                is PendingState -> div { +"Pending component" }
                is ResolvedState -> state.result?.let {
                    DndProvider {
                        backend = it.HTML5Backend
                        +props.children
                    }
                }
            }
        },
    )
}

private fun PropertiesBuilder.pairAssignmentStyles() {
    display = Display.inlineBlock
    minHeight = 100.vh
    padding = Padding(0.px, 25.px, 25.px, 25.px)
    borderStyle = LineStyle.solid
    borderTopWidth = 2.px
    borderBottomWidth = 2.px
    borderLeftWidth = 12.px
    borderRightWidth = 12.px
    borderRadius = 82.px
    borderColor = Color("#ff8c00")
    backgroundColor = Color("#faf0d2")
}

private fun PairAssignmentDocument.overlayUpdatedPlayers(players: List<Player>) = copy(
    pairs = pairs.map { pair ->
        pair.copy(
            players = pair.players.map { pinnedPlayer ->
                pinnedPlayer.copy(
                    player = players.firstOrNull { p -> p.id == pinnedPlayer.player.id }
                        ?: pinnedPlayer.player,
                )
            },
        )
    },
)

private fun notPairedPlayers(players: List<Player>, pairAssignments: PairAssignmentDocument?) =
    if (pairAssignments == null) {
        players
    } else {
        val currentlyPairedPlayerIds = pairAssignments.currentlyPairedPlayerIds()
        players.filterNot { player -> currentlyPairedPlayerIds.contains(player.id) }
    }

private fun PairAssignmentDocument.currentlyPairedPlayerIds() = pairs.flatMap { it.players }.map { it.player.id }
