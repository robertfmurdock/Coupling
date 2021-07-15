package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.json.JsonPairAssignmentDocumentRecord
import com.zegreatrob.coupling.json.toModel
import com.zegreatrob.coupling.model.TribeRecord
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.repository.pairassignmentdocument.PairAssignmentDocumentGetCurrent

interface SdkPairAssignmentDocumentGetCurrent : PairAssignmentDocumentGetCurrent, GqlQueryComponent {
    override suspend fun getCurrentPairAssignments(tribeId: TribeId): TribeRecord<PairAssignmentDocument>? =
        performQueryGetComponent(
            tribeId,
            TribeGQLComponent.CurrentPairAssignmentDocument,
            JsonPairAssignmentDocumentRecord::toModel
        )
}
