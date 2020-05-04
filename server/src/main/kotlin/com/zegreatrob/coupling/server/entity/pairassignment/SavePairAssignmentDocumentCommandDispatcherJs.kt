package com.zegreatrob.coupling.server.entity.pairassignment

import com.zegreatrob.coupling.json.toJson
import com.zegreatrob.coupling.json.toPairAssignmentDocument
import com.zegreatrob.coupling.model.pairassignmentdocument.document
import com.zegreatrob.coupling.model.tribe.with
import com.zegreatrob.coupling.server.EndpointHandlerSyntax
import com.zegreatrob.coupling.server.action.pairassignmentdocument.SavePairAssignmentDocumentCommand
import com.zegreatrob.coupling.server.action.pairassignmentdocument.SavePairAssignmentDocumentCommandDispatcher
import com.zegreatrob.coupling.server.external.express.*

interface SavePairAssignmentDocumentCommandDispatcherJs : SavePairAssignmentDocumentCommandDispatcher,
    EndpointHandlerSyntax {

    val performSavePairAssignmentDocumentCommand
        get() = endpointHandler(Response::sendSuccessful, ::handleSavePairAssignmentDocumentCommand)

    private suspend fun handleSavePairAssignmentDocumentCommand(request: Request) =
        request.savePairAssignmentDocumentCommand()
            .perform()
            .document
            .toJson()

    private fun Request.savePairAssignmentDocumentCommand() = SavePairAssignmentDocumentCommand(
        tribeId().with(
            jsonBody().toPairAssignmentDocument()
        )
    )
}
