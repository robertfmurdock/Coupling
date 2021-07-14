package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.json.toPairAssignmentDocumentRecord
import com.zegreatrob.coupling.model.TribeRecord
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.repository.pairassignmentdocument.PairAssignmentDocumentGetCurrent
import kotlin.js.Json

interface SdkPairAssignmentDocumentGetCurrent : PairAssignmentDocumentGetCurrent, GqlQueryComponent {
    override suspend fun getCurrentPairAssignments(tribeId: TribeId): TribeRecord<PairAssignmentDocument>? =
        performQueryGetComponent(tribeId, TribeGQLComponent.CurrentPairAssignmentDocument) {
            it.unsafeCast<Json?>()?.toPairAssignmentDocumentRecord()
        }
}
