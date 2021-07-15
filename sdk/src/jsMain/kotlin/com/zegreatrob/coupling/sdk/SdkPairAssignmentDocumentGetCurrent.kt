package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.json.JsonPairAssignmentDocumentRecord
import com.zegreatrob.coupling.json.couplingJsonFormat
import com.zegreatrob.coupling.json.toModel
import com.zegreatrob.coupling.model.TribeRecord
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.repository.pairassignmentdocument.PairAssignmentDocumentGetCurrent
import kotlinx.serialization.json.decodeFromDynamic

interface SdkPairAssignmentDocumentGetCurrent : PairAssignmentDocumentGetCurrent, GqlQueryComponent {
    override suspend fun getCurrentPairAssignments(tribeId: TribeId): TribeRecord<PairAssignmentDocument>? =
        performQueryGetComponent(tribeId, TribeGQLComponent.CurrentPairAssignmentDocument) {
            if (it == null)
                null
            else
                couplingJsonFormat.decodeFromDynamic<JsonPairAssignmentDocumentRecord>(it)
        }?.toModel()
}
