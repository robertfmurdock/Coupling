package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.model.party.PartyId

interface SdkPairAssignmentDocumentGetCurrent : SdkApi, GqlQueryComponent {
    override suspend fun getCurrentPairAssignments(partyId: PartyId) = performQueryGetComponent(
        partyId,
        PartyGQLComponent.CurrentPairAssignmentDocument,
    )?.partyData?.currentPairAssignmentDocument
}
