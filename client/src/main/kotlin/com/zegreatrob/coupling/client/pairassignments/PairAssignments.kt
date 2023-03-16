package com.zegreatrob.coupling.client.pairassignments

import com.zegreatrob.coupling.client.Controls
import com.zegreatrob.coupling.client.external.reactdnd.DndProvider
import com.zegreatrob.coupling.client.external.reactdndhtml5backend.HTML5Backend
import com.zegreatrob.coupling.client.pairassignments.list.DeletePairAssignmentsCommandDispatcher
import com.zegreatrob.coupling.client.player.PlayerRoster
import com.zegreatrob.coupling.components.ServerMessage
import com.zegreatrob.coupling.components.party.PartyBrowser
import com.zegreatrob.coupling.model.CouplingSocketMessage
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.minreact.DataPropsBind
import com.zegreatrob.minreact.add
import com.zegreatrob.minreact.tmFC
import csstype.Color
import csstype.Display
import csstype.LineStyle
import csstype.Padding
import csstype.PropertiesBuilder
import csstype.px
import csstype.vh
import emotion.css.ClassName
import react.dom.html.ReactHTML.div

data class PairAssignments(
    val party: Party,
    val players: List<Player>,
    val pairs: PairAssignmentDocument?,
    val setPairs: (PairAssignmentDocument) -> Unit,
    val controls: Controls<DeletePairAssignmentsCommandDispatcher>,
    val message: CouplingSocketMessage,
    val allowSave: Boolean,
) : DataPropsBind<PairAssignments>(pairAssignments)

val pairAssignmentsClassName = ClassName { pairAssignmentStyles() }

private val pairAssignments = tmFC<PairAssignments> { props ->
    val (party, players, pairs, setPairs, controls, message, allowSave) = props

    val pairAssignments = pairs?.overlayUpdatedPlayers(players)
    val notPairedPlayers = notPairedPlayers(players, pairs)

    DndProvider {
        backend = HTML5Backend
        div {
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
