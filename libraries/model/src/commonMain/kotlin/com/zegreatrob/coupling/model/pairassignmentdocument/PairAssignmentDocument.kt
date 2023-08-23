package com.zegreatrob.coupling.model.pairassignmentdocument

import com.zegreatrob.coupling.model.PartyRecord
import com.zegreatrob.coupling.model.element
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

fun List<PartyRecord<PairAssignmentDocument>>.spinsSinceLastPair(couplingPair: CouplingPair) =
    indexOfFirst { it.element.hasPair(couplingPair) }
        .takeIf { it != -1 }

fun PairAssignmentDocument.hasPair(pair: CouplingPair) = pairs.toList().any { areEqualPairs(pair, it.toPair()) }

data class PairAssignment(
    val id: PairAssignmentDocumentId,
    val document: PartyRecord<PairAssignmentDocument>? = null,
    val date: Instant? = null,
    val pairs: NotEmptyList<PinnedCouplingPair>? = null,
)
