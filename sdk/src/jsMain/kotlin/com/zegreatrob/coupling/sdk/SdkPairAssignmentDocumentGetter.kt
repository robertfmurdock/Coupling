package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.json.toPairAssignmentDocument
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocumentGetter
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.sdk.external.axios.getList
import kotlinx.coroutines.await
import kotlin.js.Json

interface SdkPairAssignmentDocumentGetter : PairAssignmentDocumentGetter, AxiosSyntax {
    override suspend fun getPairAssignments(tribeId: TribeId): List<PairAssignmentDocument> =
        axios.getList("/api/${tribeId.value}/history")
            .then { it.map(Json::toPairAssignmentDocument) }
            .await()
}