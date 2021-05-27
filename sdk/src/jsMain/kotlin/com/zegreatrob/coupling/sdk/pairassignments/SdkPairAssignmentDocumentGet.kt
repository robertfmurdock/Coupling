package com.zegreatrob.coupling.sdk.pairassignments

import com.zegreatrob.coupling.json.recordFor
import com.zegreatrob.coupling.json.toPairAssignmentDocument
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.pairassignmentdocument.TribeIdPairAssignmentDocument
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.model.tribe.with
import com.zegreatrob.coupling.repository.pairassignmentdocument.PairAssignmentDocumentGet
import com.zegreatrob.coupling.sdk.GqlQueryComponent
import com.zegreatrob.coupling.sdk.TribeGQLComponent.PairAssignmentDocumentList
import kotlin.js.Json

interface SdkPairAssignmentDocumentGet : PairAssignmentDocumentGet, GqlQueryComponent {
    override suspend fun getPairAssignments(tribeId: TribeId): List<Record<TribeIdPairAssignmentDocument>> =
        performQueryGetComponent(tribeId, PairAssignmentDocumentList) {
            it.unsafeCast<Array<Json>?>()
                ?.map { recordJson -> recordJson.recordFor(tribeId.with(recordJson.toPairAssignmentDocument())) }
        }
            ?: emptyList()
}
