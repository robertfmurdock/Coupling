package com.zegreatrob.coupling.sdk.pairassignments

import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocumentId
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.repository.pairassignmentdocument.PairAssignmentDocumentDelete
import com.zegreatrob.coupling.sdk.AxiosSyntax
import kotlinx.coroutines.asDeferred
import kotlin.js.Promise

interface SdkPairAssignmentDocumentDelete : PairAssignmentDocumentDelete, AxiosSyntax {
    override suspend fun delete(
        tribeId: TribeId,
        pairAssignmentDocumentId: PairAssignmentDocumentId
    ): Boolean {
        axios.delete("/api/tribes/${tribeId.value}/history/${pairAssignmentDocumentId.value}")
            .unsafeCast<Promise<Unit>>()
            .asDeferred()
            .await()
        return true
    }
}