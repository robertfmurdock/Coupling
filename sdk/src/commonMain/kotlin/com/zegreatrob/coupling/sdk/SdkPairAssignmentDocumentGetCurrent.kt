package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.json.toDomain
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.repository.pairassignmentdocument.PairAssignmentDocumentGetCurrent

interface SdkPairAssignmentDocumentGetCurrent : PairAssignmentDocumentGetCurrent, GqlQueryComponent {
    override suspend fun getCurrentPairAssignments(partyId: PartyId) = performQueryGetComponent(
        partyId,
        PartyGQLComponent.CurrentPairAssignmentDocument,
    ) { it?.toDomain()?.partyData?.currentPairAssignmentDocument }
}
