package com.zegreatrob.coupling.model.pairassignmentdocument

import com.zegreatrob.coupling.model.PartyRecord
import com.zegreatrob.coupling.model.player.PlayerId
import kotools.types.collection.NotEmptyList
import kotlin.time.Instant

data class PairAssignment(
    val documentId: PairAssignmentDocumentId,
    val playerIds: List<PlayerId>,
    val details: PartyRecord<PairAssignmentDocument>? = null,
    val date: Instant,
    val allPairs: NotEmptyList<PinnedCouplingPair>? = null,
    val recentTimesPaired: Int? = null,
)
