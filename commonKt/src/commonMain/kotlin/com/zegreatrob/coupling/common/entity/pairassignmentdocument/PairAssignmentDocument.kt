package com.zegreatrob.coupling.common.entity.pairassignmentdocument

import com.soywiz.klock.DateTime
import com.zegreatrob.coupling.common.entity.tribe.TribeId

data class PairAssignmentDocument(
        val date: DateTime,
        val pairs: List<PinnedCouplingPair>,
        val id: PairAssignmentDocumentId? = null
)

fun PairAssignmentDocument.with(tribeId: TribeId) = TribeIdPairAssignmentDocument(tribeId, this)

data class TribeIdPairAssignmentDocument(val tribeId: TribeId, val document: PairAssignmentDocument)