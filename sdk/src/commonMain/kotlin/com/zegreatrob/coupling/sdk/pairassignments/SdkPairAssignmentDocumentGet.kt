package com.zegreatrob.coupling.sdk.pairassignments

import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.repository.pairassignmentdocument.PairAssignmentDocumentGet
import com.zegreatrob.coupling.sdk.GqlQueryComponent
import com.zegreatrob.coupling.sdk.PartyGQLComponent.PairAssignmentDocumentList
import com.zegreatrob.coupling.sdk.performQueryGetComponent

interface SdkPairAssignmentDocumentGet : PairAssignmentDocumentGet, GqlQueryComponent {
    override suspend fun getPairAssignments(partyId: PartyId) =
        performQueryGetComponent(partyId, PairAssignmentDocumentList)
            ?.partyData
            ?.pairAssignmentDocumentList
            ?: emptyList()
}
