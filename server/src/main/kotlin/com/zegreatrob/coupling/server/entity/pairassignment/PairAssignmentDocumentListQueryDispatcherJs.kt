package com.zegreatrob.coupling.server.entity.pairassignment

import com.zegreatrob.coupling.json.toJson
import com.zegreatrob.coupling.server.action.pairassignmentdocument.PairAssignmentDocumentListQuery
import com.zegreatrob.coupling.server.action.pairassignmentdocument.PairAssignmentDocumentListQueryDispatcher
import com.zegreatrob.coupling.server.entity.tribe.ScopeSyntax

interface PairAssignmentDocumentListQueryDispatcherJs : PairAssignmentDocumentListQueryDispatcher, ScopeSyntax {

    suspend fun performPairAssignmentDocumentListQueryGQL() = PairAssignmentDocumentListQuery
        .perform()
        .map { it.toJson() }
        .toTypedArray()
}
