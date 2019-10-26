package com.zegreatrob.coupling.client.sdk

import com.zegreatrob.coupling.client.external.axios.axios
import com.zegreatrob.coupling.client.external.axios.getList
import com.zegreatrob.coupling.core.entity.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.core.entity.tribe.TribeId
import com.zegreatrob.coupling.common.toPairAssignmentDocument
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.asDeferred
import kotlin.js.Json

interface GetPairAssignmentListSyntax {

    fun TribeId.getPairAssignmentListAsync(): Deferred<List<PairAssignmentDocument>> =
        axios.getList("/api/$value/history")
            .then { it.map(Json::toPairAssignmentDocument) }
            .asDeferred()

}