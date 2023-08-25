package com.zegreatrob.coupling.model.pairassignmentdocument

import com.zegreatrob.coupling.model.PartyRecord
import kotlinx.datetime.Instant
import kotools.types.collection.NotEmptyList

data class PairAssignment(
    val playerIds: List<String>? = null,
    val documentId: PairAssignmentDocumentId? = null,
    val details: PartyRecord<PairAssignmentDocument>? = null,
    val date: Instant? = null,
    val allPairs: NotEmptyList<PinnedCouplingPair>? = null,
    val recentTimesPaired: Int? = null,
)
