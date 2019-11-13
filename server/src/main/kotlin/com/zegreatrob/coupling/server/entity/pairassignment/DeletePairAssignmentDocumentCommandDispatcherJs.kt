package com.zegreatrob.coupling.server.entity.pairassignment

import com.zegreatrob.coupling.server.EndpointHandlerSyntax
import com.zegreatrob.coupling.server.JsonSendToResponseSyntax
import com.zegreatrob.coupling.server.action.pairassignmentdocument.DeletePairAssignmentDocumentCommand
import com.zegreatrob.coupling.server.action.pairassignmentdocument.DeletePairAssignmentDocumentCommandDispatcher
import com.zegreatrob.coupling.server.ResponseHelpers.sendSuccess
import com.zegreatrob.coupling.server.entity.tribe.RequestTribeIdSyntax

interface DeletePairAssignmentDocumentCommandDispatcherJs : DeletePairAssignmentDocumentCommandDispatcher,
    RequestTribeIdSyntax, RequestPairAssignmentDocumentIdSyntax, JsonSendToResponseSyntax, EndpointHandlerSyntax {
    @JsName("performDeletePairAssignmentDocumentCommand")
    val performDeletePairAssignmentDocumentCommand
        get() = endpointHandler(sendSuccess("Pair Assignments")) {
            DeletePairAssignmentDocumentCommand(tribeId(), pairAssignmentDocumentId())
                .perform()
        }
}

