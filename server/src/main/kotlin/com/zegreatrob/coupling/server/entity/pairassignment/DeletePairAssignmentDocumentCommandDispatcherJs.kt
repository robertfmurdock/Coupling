package com.zegreatrob.coupling.server.entity.pairassignment

import com.zegreatrob.coupling.server.JsonSendToResponseSyntax
import com.zegreatrob.coupling.server.PerformJsonHandlingSyntax
import com.zegreatrob.coupling.server.action.pairassignmentdocument.DeletePairAssignmentDocumentCommand
import com.zegreatrob.coupling.server.action.pairassignmentdocument.DeletePairAssignmentDocumentCommandDispatcher
import com.zegreatrob.coupling.server.entity.tribe.RequestTribeIdSyntax
import com.zegreatrob.coupling.server.external.express.Request
import com.zegreatrob.coupling.server.external.express.Response
import kotlin.js.json

interface DeletePairAssignmentDocumentCommandDispatcherJs : DeletePairAssignmentDocumentCommandDispatcher,
    RequestTribeIdSyntax, RequestPairAssignmentDocumentIdSyntax, JsonSendToResponseSyntax, PerformJsonHandlingSyntax {
    @JsName("performDeletePairAssignmentDocumentCommand")
    fun performDeletePairAssignmentDocumentCommand(request: Request, response: Response) =
        performJsonHandling(request, sendSuccess(response), ::handleDeletePairAssignmentDocumentCommand)

    private fun sendSuccess(response: Response) = { result: Boolean ->
        if (result) {
            json("message" to "SUCCESS")
                .sendTo(response)
        } else {
            json("message" to "Pair Assignments could not be deleted because they do not exist.")
                .sendTo(response, 404)
        }
    }

    private suspend fun handleDeletePairAssignmentDocumentCommand(request: Request) =
        DeletePairAssignmentDocumentCommand(request.tribeId(), request.pairAssignmentDocumentId())
            .perform()

}
