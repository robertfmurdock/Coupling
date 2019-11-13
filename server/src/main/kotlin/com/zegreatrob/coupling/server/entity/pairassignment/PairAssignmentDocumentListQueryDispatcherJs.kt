package com.zegreatrob.coupling.server.entity.pairassignment

import com.zegreatrob.coupling.json.toJson
import com.zegreatrob.coupling.server.EndpointHandlerSyntax
import com.zegreatrob.coupling.server.action.pairassignmentdocument.PairAssignmentDocumentListQuery
import com.zegreatrob.coupling.server.action.pairassignmentdocument.PairAssignmentDocumentListQueryDispatcher
import com.zegreatrob.coupling.server.entity.tribe.RequestTribeIdSyntax
import com.zegreatrob.coupling.server.external.express.Response
import com.zegreatrob.coupling.server.external.express.sendSuccessful

interface PairAssignmentDocumentListQueryDispatcherJs : PairAssignmentDocumentListQueryDispatcher, RequestTribeIdSyntax,
    EndpointHandlerSyntax {
    @JsName("performPairAssignmentDocumentListQuery")
    val performPairAssignmentDocumentListQuery
        get() = endpointHandler(Response::sendSuccessful) {
            PairAssignmentDocumentListQuery(tribeId())
                .perform()
                .map { it.toJson() }
                .toTypedArray()
        }
}
