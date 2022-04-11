package com.zegreatrob.coupling.model.pairassignmentdocument

import com.zegreatrob.coupling.model.tribe.PartyId
import kotlin.jvm.JvmInline

@JvmInline
value class PairAssignmentDocumentId(val value: String)

data class TribeIdPairAssignmentDocumentId(val partyId: PartyId, val pairAssignmentDocumentId: PairAssignmentDocumentId)