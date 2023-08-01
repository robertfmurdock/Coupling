package com.zegreatrob.coupling.model.pairassignmentdocument

import com.zegreatrob.coupling.model.map
import com.zegreatrob.coupling.model.party.PartyElement
import com.zegreatrob.coupling.model.player.Player
import kotlinx.datetime.Instant
import kotools.types.collection.NotEmptyList

data class PairAssignmentDocument(
    val id: PairAssignmentDocumentId,
    val date: Instant,
    val pairs: NotEmptyList<PinnedCouplingPair>,
    val discordMessageId: String? = null,
    val slackMessageId: String? = null,
)

fun PairAssignmentDocument.orderedPairedPlayers(): List<Player> = pairs
    .map(PinnedCouplingPair::players)
    .map(NotEmptyList<Player>::toList)
    .toList()
    .flatten()

val PartyElement<PairAssignmentDocument>.document get() = element
