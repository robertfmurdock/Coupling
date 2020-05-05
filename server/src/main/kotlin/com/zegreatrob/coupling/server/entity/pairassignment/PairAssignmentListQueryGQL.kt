package com.zegreatrob.coupling.server.entity.pairassignment

import com.zegreatrob.coupling.json.toJson
import com.zegreatrob.coupling.model.pairassignmentdocument.document
import com.zegreatrob.coupling.server.action.pairassignmentdocument.PairAssignmentDocumentListQuery
import com.zegreatrob.coupling.server.action.pairassignmentdocument.PairAssignmentDocumentListQueryDispatcher

suspend fun PairAssignmentDocumentListQueryDispatcher.performPairAssignmentListQueryGQL() =
    PairAssignmentDocumentListQuery
        .perform()
        .map { it.toJson().add(it.data.document.toJson()) }
        .toTypedArray()
