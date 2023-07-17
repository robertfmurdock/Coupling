package com.zegreatrob.coupling.model.pairassignmentdocument

import com.zegreatrob.coupling.model.party.PartyElement
import kotlinx.datetime.Instant
import kotools.types.collection.NotEmptyList

data class PairAssignmentDocument(
    val id: PairAssignmentDocumentId,
    val date: Instant,
    val pairs: NotEmptyList<PinnedCouplingPair>,
)

fun PairAssignmentDocument.orderedPairedPlayers() = pairs.toList().flatMap(PinnedCouplingPair::players)

val PartyElement<PairAssignmentDocument>.document get() = element
