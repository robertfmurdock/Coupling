package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.json.toJson
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocumentSaver
import com.zegreatrob.coupling.model.pairassignmentdocument.TribeIdPairAssignmentDocument

interface SdkPairAssignmentDocumentSaver : PairAssignmentDocumentSaver, AxiosSyntax {
    override suspend fun save(tribeIdPairAssignmentDocument: TribeIdPairAssignmentDocument) {
        val (tribeId, pairAssignmentDocument) = tribeIdPairAssignmentDocument
        axios.postAsync<Unit>("/api/${tribeId.value}/history", pairAssignmentDocument.toJson())
            .await()
    }
}