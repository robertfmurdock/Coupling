package com.zegreatrob.coupling.model.pairassignmentdocument

import com.soywiz.klock.DateTime
import com.zegreatrob.coupling.model.tribe.TribeElement

data class PairAssignmentDocument(
    val id: PairAssignmentDocumentId? = null,
    val date: DateTime,
    val pairs: List<PinnedCouplingPair>
)

fun PairAssignmentDocument.orderedPairedPlayers() = pairs
    .asSequence()
    .flatMap { it.players.asSequence() }
    .map { it.player }

typealias TribeIdPairAssignmentDocument = TribeElement<PairAssignmentDocument>

val TribeIdPairAssignmentDocument.tribeId get() = id
val TribeIdPairAssignmentDocument.document get() = element