package com.zegreatrob.coupling.model.pairassignmentdocument

import com.soywiz.klock.DateTime
import com.zegreatrob.coupling.model.tribe.PartyElement

data class PairAssignmentDocument(
    val id: PairAssignmentDocumentId,
    val date: DateTime,
    val pairs: List<PinnedCouplingPair>
)

fun PairAssignmentDocument.orderedPairedPlayers() = pairs
    .asSequence()
    .flatMap { it.players.asSequence() }
    .map { it.player }

typealias TribeIdPairAssignmentDocument = PartyElement<PairAssignmentDocument>

val TribeIdPairAssignmentDocument.tribeId get() = id
val TribeIdPairAssignmentDocument.document get() = element
