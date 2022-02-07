package com.zegreatrob.coupling.model.pairassignmentdocument

import com.zegreatrob.coupling.model.tribe.TribeId
import kotlin.jvm.JvmInline

@JvmInline
value class PairAssignmentDocumentId(val value: String)

data class TribeIdPairAssignmentDocumentId(val tribeId: TribeId, val pairAssignmentDocumentId: PairAssignmentDocumentId)