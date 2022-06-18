package com.zegreatrob.coupling.client.pairassignments

import com.zegreatrob.coupling.client.Controls
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.client.external.reactdnd.DndProvider
import com.zegreatrob.coupling.client.external.reactdndhtml5backend.HTML5Backend
import com.zegreatrob.coupling.client.pairassignments.list.DeletePairAssignmentsCommandDispatcher
import com.zegreatrob.coupling.client.party.PartyBrowser
import com.zegreatrob.coupling.client.player.PlayerRoster
import com.zegreatrob.coupling.client.user.ServerMessage
import com.zegreatrob.coupling.model.CouplingSocketMessage
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.repository.pairassignmentdocument.PairAssignmentDocumentRepository
import com.zegreatrob.minreact.DataPropsBind
import com.zegreatrob.minreact.add
import com.zegreatrob.minreact.tmFC
import react.dom.html.ReactHTML.div
import react.key

interface PairAssignmentsCommandDispatcher :
    SavePairAssignmentsCommandDispatcher,
    DeletePairAssignmentsCommandDispatcher {
    override val pairAssignmentDocumentRepository: PairAssignmentDocumentRepository
}

data class PairAssignments(
    val party: Party,
    val players: List<Player>,
    val pairs: PairAssignmentDocument?,
    val setPairs: (PairAssignmentDocument) -> Unit,
    val controls: Controls<DeletePairAssignmentsCommandDispatcher>,
    val message: CouplingSocketMessage,
    val allowSave: Boolean
) : DataPropsBind<PairAssignments>(pairAssignments)

private val styles = useStyles("pairassignments/PairAssignments")

private val pairAssignments = tmFC<PairAssignments> { props ->
    val (party, players, pairs, setPairs, controls, message, allowSave) = props

    val pairAssignments = pairs?.overlayUpdatedPlayers(players)
    val notPairedPlayers = notPairedPlayers(players, pairs)

    DndProvider {
        backend = HTML5Backend
        div {
            className = styles.className
            div {
                add(PartyBrowser(party))
                add(PairSection(party, players, pairAssignments, allowSave, setPairs, controls))
            }
            add(ControlPanel(party))
            add(PlayerRoster(label = "Unpaired players", partyId = party.id, players = notPairedPlayers))
            add(ServerMessage(message)) { key = "${message.text} ${message.players.size}" }
        }
    }
}

private fun PairAssignmentDocument.overlayUpdatedPlayers(players: List<Player>) = copy(
    pairs = pairs.map { pair ->
        pair.copy(
            players = pair.players.map { pinnedPlayer ->
                pinnedPlayer.copy(
                    player = players.firstOrNull { p -> p.id == pinnedPlayer.player.id }
                        ?: pinnedPlayer.player
                )
            }
        )
    }
)

private fun notPairedPlayers(players: List<Player>, pairAssignments: PairAssignmentDocument?) =
    if (pairAssignments == null) {
        players
    } else {
        val currentlyPairedPlayerIds = pairAssignments.currentlyPairedPlayerIds()
        players.filterNot { player -> currentlyPairedPlayerIds.contains(player.id) }
    }

private fun PairAssignmentDocument.currentlyPairedPlayerIds() = pairs.flatMap { it.players }.map { it.player.id }
