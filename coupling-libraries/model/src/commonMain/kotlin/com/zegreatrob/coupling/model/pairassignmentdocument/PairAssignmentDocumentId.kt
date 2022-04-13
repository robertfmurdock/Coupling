package com.zegreatrob.coupling.model.pairassignmentdocument

import com.zegreatrob.coupling.model.party.PartyId
import kotlin.jvm.JvmInline

@JvmInline
value class PairAssignmentDocumentId(val value: String)

data class PartyIdPairAssignmentDocumentId(val partyId: PartyId, val pairAssignmentDocumentId: PairAssignmentDocumentId)