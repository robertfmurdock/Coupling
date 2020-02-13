package com.zegreatrob.coupling.model.pairassignmentdocument

import com.soywiz.klock.DateTime
import com.zegreatrob.coupling.model.tribe.TribeId

data class PairAssignmentDocument(
    val id: PairAssignmentDocumentId? = null,
    val date: DateTime,
    val pairs: List<PinnedCouplingPair>
)

fun PairAssignmentDocument.with(tribeId: TribeId) =
    TribeIdPairAssignmentDocument(tribeId, this)

fun PairAssignmentDocument.orderedPairedPlayers() = pairs
    .asSequence()
    .flatMap { it.players.asSequence() }
    .map { it.player }

data class TribeIdPairAssignmentDocument(val tribeId: TribeId, val document: PairAssignmentDocument)