package com.zegreatrob.coupling.sdk.pairassignments

import com.zegreatrob.coupling.json.JsonPairAssignmentDocumentRecord
import com.zegreatrob.coupling.json.couplingJsonFormat
import com.zegreatrob.coupling.json.toModel
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.pairassignmentdocument.TribeIdPairAssignmentDocument
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.repository.pairassignmentdocument.PairAssignmentDocumentGet
import com.zegreatrob.coupling.sdk.GqlQueryComponent
import com.zegreatrob.coupling.sdk.TribeGQLComponent.PairAssignmentDocumentList
import kotlinx.serialization.json.decodeFromDynamic

interface SdkPairAssignmentDocumentGet : PairAssignmentDocumentGet, GqlQueryComponent {
    override suspend fun getPairAssignments(tribeId: TribeId): List<Record<TribeIdPairAssignmentDocument>> =
        performQueryGetComponent(tribeId, PairAssignmentDocumentList) {
            couplingJsonFormat.decodeFromDynamic<List<JsonPairAssignmentDocumentRecord>>(it)
        }?.map(JsonPairAssignmentDocumentRecord::toModel) ?: emptyList()
}
