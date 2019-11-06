package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.json.toJson
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocumentSaver
import com.zegreatrob.coupling.model.pairassignmentdocument.TribeIdPairAssignmentDocument
import kotlinx.coroutines.asDeferred
import kotlin.js.Promise

interface SdkPairAssignmentDocumentSaver : PairAssignmentDocumentSaver, AxiosSyntax {
    override suspend fun save(tribeIdPairAssignmentDocument: TribeIdPairAssignmentDocument) {
        val (tribeId, pairAssignmentDocument) = tribeIdPairAssignmentDocument
        axios.post("/api/${tribeId.value}/history", pairAssignmentDocument.toJson())
            .unsafeCast<Promise<Unit>>()
            .asDeferred()
            .await()
    }
}