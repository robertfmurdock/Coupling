package com.zegreatrob.coupling.server.entity.pairassignment

import com.zegreatrob.coupling.json.toJson
import com.zegreatrob.coupling.server.JsonSendToResponseSyntax
import com.zegreatrob.coupling.server.action.pairassignmentdocument.PairAssignmentDocumentListQuery
import com.zegreatrob.coupling.server.action.pairassignmentdocument.PairAssignmentDocumentListQueryDispatcher
import com.zegreatrob.coupling.server.entity.tribe.RequestTribeIdSyntax
import com.zegreatrob.coupling.server.entity.tribe.ScopeSyntax
import com.zegreatrob.coupling.server.external.express.Request
import com.zegreatrob.coupling.server.external.express.Response
import kotlinx.coroutines.promise

interface PairAssignmentDocumentListQueryDispatcherJs : PairAssignmentDocumentListQueryDispatcher, ScopeSyntax,
    RequestTribeIdSyntax, JsonSendToResponseSyntax {
    @JsName("performPairAssignmentDocumentListQuery")
    fun performPairAssignmentDocumentListQuery(request: Request, response: Response) = scope.promise {
        PairAssignmentDocumentListQuery(request.tribeId())
            .perform()
            .map { it.toJson() }
            .toTypedArray()
            .sendTo(response)
    }
}
