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
        performJsonHandling(
            request,
            response,
            sendSuccess("Pair Assignments"),
            ::handleDeletePairAssignmentDocumentCommand
        )

    private fun sendSuccess(entityName: String): Response.(Boolean) -> Unit = { result: Boolean ->
        if (result) {
            json("message" to "SUCCESS")
                .sendTo(this)
        } else {
            json(
                "message" to "$entityName could not be deleted because they do not exist."
            )
                .sendTo(this, 404)
        }
    }

    private suspend fun handleDeletePairAssignmentDocumentCommand(request: Request) =
        DeletePairAssignmentDocumentCommand(request.tribeId(), request.pairAssignmentDocumentId())
            .perform()

}
