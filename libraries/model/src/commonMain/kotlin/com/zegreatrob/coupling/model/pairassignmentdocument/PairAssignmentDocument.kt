package com.zegreatrob.coupling.model.pairassignmentdocument

import com.zegreatrob.coupling.model.party.PartyElement
import korlibs.time.DateTime

data class PairAssignmentDocument(
    val id: PairAssignmentDocumentId,
    val date: DateTime,
    val pairs: List<PinnedCouplingPair>,
)

fun PairAssignmentDocument.orderedPairedPlayers() = pairs.flatMap(PinnedCouplingPair::players)

val PartyElement<PairAssignmentDocument>.document get() = element
