package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.json.JsonPairAssignmentDocumentRecord
import com.zegreatrob.coupling.json.toModel
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.repository.pairassignmentdocument.PairAssignmentDocumentGetCurrent

interface SdkPairAssignmentDocumentGetCurrent : PairAssignmentDocumentGetCurrent, GqlQueryComponent {
    override suspend fun getCurrentPairAssignments(tribeId: PartyId) = performQueryGetComponent(
        tribeId,
        TribeGQLComponent.CurrentPairAssignmentDocument,
        JsonPairAssignmentDocumentRecord::toModel
    )
}
