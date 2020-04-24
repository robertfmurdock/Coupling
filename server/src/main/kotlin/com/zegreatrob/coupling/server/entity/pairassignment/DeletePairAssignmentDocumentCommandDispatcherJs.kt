package com.zegreatrob.coupling.server.entity.pairassignment

import com.zegreatrob.coupling.server.EndpointHandlerSyntax
import com.zegreatrob.coupling.server.ResponseHelpers.sendDeleteResults
import com.zegreatrob.coupling.server.action.pairassignmentdocument.DeletePairAssignmentDocumentCommand
import com.zegreatrob.coupling.server.action.pairassignmentdocument.DeletePairAssignmentDocumentCommandDispatcher
import com.zegreatrob.coupling.server.entity.tribe.RequestTribeIdSyntax

interface DeletePairAssignmentDocumentCommandDispatcherJs : DeletePairAssignmentDocumentCommandDispatcher,
    RequestTribeIdSyntax, RequestPairAssignmentDocumentIdSyntax, EndpointHandlerSyntax {

    val performDeletePairAssignmentDocumentCommand
        get() = endpointHandler(sendDeleteResults("Pair Assignments")) {
            DeletePairAssignmentDocumentCommand(tribeId(), pairAssignmentDocumentId())
                .perform()
        }
}

