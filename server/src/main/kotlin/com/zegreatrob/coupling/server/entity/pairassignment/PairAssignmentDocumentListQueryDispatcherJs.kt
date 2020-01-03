package com.zegreatrob.coupling.server.entity.pairassignment

import com.zegreatrob.coupling.json.toJson
import com.zegreatrob.coupling.server.action.pairassignmentdocument.PairAssignmentDocumentListQuery
import com.zegreatrob.coupling.server.action.pairassignmentdocument.PairAssignmentDocumentListQueryDispatcher

interface PairAssignmentDocumentListQueryDispatcherJs : PairAssignmentDocumentListQueryDispatcher {

    suspend fun performPairAssignmentDocumentListQueryGQL() = PairAssignmentDocumentListQuery
        .perform()
        .map { it.toJson() }
        .toTypedArray()
}
