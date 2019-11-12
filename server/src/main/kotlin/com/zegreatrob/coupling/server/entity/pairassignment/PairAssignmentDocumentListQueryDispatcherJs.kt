package com.zegreatrob.coupling.server.entity.pairassignment

import com.zegreatrob.coupling.json.toJson
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.server.action.pairassignmentdocument.PairAssignmentDocumentListQuery
import com.zegreatrob.coupling.server.action.pairassignmentdocument.PairAssignmentDocumentListQueryDispatcher
import com.zegreatrob.coupling.server.entity.tribe.ScopeSyntax
import kotlinx.coroutines.promise

interface PairAssignmentDocumentListQueryDispatcherJs : PairAssignmentDocumentListQueryDispatcher, ScopeSyntax {
    @JsName("performPairAssignmentDocumentListQuery")
    fun performPairAssignmentDocumentListQuery(tribeId: String) = scope.promise {
        PairAssignmentDocumentListQuery(TribeId(tribeId))
            .perform()
            .map { it.toJson() }
            .toTypedArray()
    }
}
