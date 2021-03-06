package com.zegreatrob.coupling.sdk.pairassignments

import com.zegreatrob.coupling.json.JsonPairAssignmentDocumentRecord
import com.zegreatrob.coupling.json.toModel
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.repository.pairassignmentdocument.PairAssignmentDocumentGet
import com.zegreatrob.coupling.sdk.GqlQueryComponent
import com.zegreatrob.coupling.sdk.TribeGQLComponent.PairAssignmentDocumentList
import com.zegreatrob.coupling.sdk.performQueryGetComponent

interface SdkPairAssignmentDocumentGet : PairAssignmentDocumentGet, GqlQueryComponent {
    override suspend fun getPairAssignments(tribeId: TribeId) = performQueryGetComponent(
        tribeId, PairAssignmentDocumentList, ::toModel
    ) ?: emptyList()

    private fun toModel(list: List<JsonPairAssignmentDocumentRecord>) =
        list.map(JsonPairAssignmentDocumentRecord::toModel)
}
