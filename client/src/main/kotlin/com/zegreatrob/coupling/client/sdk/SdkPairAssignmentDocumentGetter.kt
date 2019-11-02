package com.zegreatrob.coupling.client.sdk

import com.zegreatrob.coupling.client.external.axios.axios
import com.zegreatrob.coupling.client.external.axios.getList
import com.zegreatrob.coupling.json.toPairAssignmentDocument
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocumentGetter
import com.zegreatrob.coupling.model.tribe.TribeId
import kotlinx.coroutines.asDeferred
import kotlin.js.Json

interface SdkPairAssignmentDocumentGetter :
    PairAssignmentDocumentGetter {
    override fun getPairAssignmentsAsync(tribeId: TribeId) = axios.getList("/api/${tribeId.value}/history")
        .then { it.map(Json::toPairAssignmentDocument) }
        .asDeferred()
}