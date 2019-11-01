package com.zegreatrob.coupling.client.sdk

import com.zegreatrob.coupling.client.external.axios.axios
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.json.toJson
import kotlinx.coroutines.asDeferred
import kotlin.js.Promise

interface PairAssignmentSaveSyntax {

    suspend fun saveAsync(tribeId: TribeId, pairAssignmentDocument: PairAssignmentDocument) =
        axios.post("/api/${tribeId.value}/history", pairAssignmentDocument.toJson())
            .unsafeCast<Promise<Unit>>()
            .asDeferred()
            .await()

}
