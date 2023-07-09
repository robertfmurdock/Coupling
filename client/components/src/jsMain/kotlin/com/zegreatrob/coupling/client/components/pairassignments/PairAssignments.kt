package com.zegreatrob.coupling.client.components.pairassignments

import com.zegreatrob.coupling.action.pairassignmentdocument.DeletePairAssignmentsCommand
import com.zegreatrob.coupling.client.components.Controls
import com.zegreatrob.coupling.client.components.ServerMessage
import com.zegreatrob.coupling.client.components.external.reactdnd.DndProvider
import com.zegreatrob.coupling.client.components.external.reactdndhtml5backend.html5BackendDeferred
import com.zegreatrob.coupling.client.components.party.PartyBrowser
import com.zegreatrob.coupling.client.components.player.PlayerRoster
import com.zegreatrob.coupling.model.CouplingSocketMessage
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pairassignmentdocument.PinnedCouplingPair
import com.zegreatrob.coupling.model.pairassignmentdocument.players
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.minreact.ReactFunc
import com.zegreatrob.minreact.add
import com.zegreatrob.minreact.nfc
import com.zegreatrob.react.dataloader.DataLoader
import com.zegreatrob.react.dataloader.EmptyState
import com.zegreatrob.react.dataloader.PendingState
import com.zegreatrob.react.dataloader.ResolvedState
import csstype.PropertiesBuilder
import emotion.css.ClassName
import react.Props
import react.PropsWithChildren
import react.dom.html.ReactHTML.div
import web.cssom.Color
import web.cssom.Display
import web.cssom.LineStyle
import web.cssom.Padding
import web.cssom.px
import web.cssom.vh

val pairAssignmentsClassName = ClassName { pairAssignmentStyles() }

external interface PairAssignmentsProps : Props {
    var party: PartyDetails
    var players: List<Player>
    var pairs: PairAssignmentDocument?
    var setPairs: (PairAssignmentDocument) -> Unit
    var controls: Controls<DeletePairAssignmentsCommand.Dispatcher>
    var message: CouplingSocketMessage
    var allowSave: Boolean
}

@ReactFunc
val PairAssignments by nfc<PairAssignmentsProps> { props ->
    val (party, players, pairs, setPairs, controls, message, allowSave) = props

    val pairAssignments = pairs?.overlayUpdatedPlayers(players)
    val notPairedPlayers = notPairedPlayers(players, pairs)

    Html5DndProvider {
        div {
            className = pairAssignmentsClassName
            div {
                PartyBrowser(party)
                PairSection(party, players, pairAssignments, allowSave, setPairs, controls)
            }
            ControlPanel(party)
            add(PlayerRoster(label = "Unpaired players", partyId = party.id, players = notPairedPlayers))
            ServerMessage(message, key = "${message.text} ${message.players.size}")
        }
    }
}

val Html5DndProvider by nfc<PropsWithChildren> { props ->
    add(
        DataLoader({ html5BackendDeferred.await() }, { null }) { state ->
            when (state) {
                is EmptyState -> div { +"Preparing component" }
                is PendingState -> div { +"Pending component" }
                is ResolvedState -> state.result?.let {
                    DndProvider {
                        backend = it.html5Backend
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
            pinnedPlayers = pair.pinnedPlayers.map { pinnedPlayer ->
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

private fun PairAssignmentDocument.currentlyPairedPlayerIds() =
    pairs.flatMap(PinnedCouplingPair::players)
        .map(Player::id)
