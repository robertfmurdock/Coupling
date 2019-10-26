package com.zegreatrob.coupling.core.entity.pairassignmentdocument

import com.soywiz.klock.DateTime
import com.zegreatrob.coupling.core.entity.tribe.TribeId

data class PairAssignmentDocument(
        val date: DateTime,
        val pairs: List<PinnedCouplingPair>,
        val id: PairAssignmentDocumentId? = null
)

fun PairAssignmentDocument.with(tribeId: TribeId) = TribeIdPairAssignmentDocument(tribeId, this)

data class TribeIdPairAssignmentDocument(val tribeId: TribeId, val document: PairAssignmentDocument)