package com.zegreatrob.coupling.server.entity.pairassignment

import com.zegreatrob.coupling.json.toJson
import com.zegreatrob.coupling.json.toPairAssignmentDocument
import com.zegreatrob.coupling.model.pairassignmentdocument.TribeIdPairAssignmentDocument
import com.zegreatrob.coupling.server.PerformJsonHandlingSyntax
import com.zegreatrob.coupling.server.action.pairassignmentdocument.SavePairAssignmentDocumentCommand
import com.zegreatrob.coupling.server.action.pairassignmentdocument.SavePairAssignmentDocumentCommandDispatcher
import com.zegreatrob.coupling.server.entity.tribe.RequestTribeIdSyntax
import com.zegreatrob.coupling.server.external.express.Request
import com.zegreatrob.coupling.server.external.express.Response
import com.zegreatrob.coupling.server.external.express.jsonBody
import com.zegreatrob.coupling.server.external.express.sendSuccessful

interface SavePairAssignmentDocumentCommandDispatcherJs : SavePairAssignmentDocumentCommandDispatcher,
    RequestTribeIdSyntax, PerformJsonHandlingSyntax {

    @JsName("performSavePairAssignmentDocumentCommand")
    fun performSavePairAssignmentDocumentCommand(request: Request, response: Response) =
        performJsonHandling(request, response, Response::sendSuccessful, ::handleSavePairAssignmentDocumentCommand)

    private suspend fun handleSavePairAssignmentDocumentCommand(request: Request) =
        request.savePairAssignmentDocumentCommand()
            .perform()
            .document
            .toJson()

    private fun Request.savePairAssignmentDocumentCommand() = SavePairAssignmentDocumentCommand(
        TribeIdPairAssignmentDocument(
            tribeId(),
            jsonBody().toPairAssignmentDocument()
        )
    )
}
