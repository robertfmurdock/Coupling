package com.zegreatrob.coupling.server.entity.pairassignment

import com.zegreatrob.coupling.server.EndpointHandlerSyntax
import com.zegreatrob.coupling.server.JsonSendToResponseSyntax
import com.zegreatrob.coupling.server.action.pairassignmentdocument.DeletePairAssignmentDocumentCommand
import com.zegreatrob.coupling.server.action.pairassignmentdocument.DeletePairAssignmentDocumentCommandDispatcher
import com.zegreatrob.coupling.server.entity.tribe.RequestTribeIdSyntax
import com.zegreatrob.coupling.server.external.express.Response
import kotlin.js.json

interface DeletePairAssignmentDocumentCommandDispatcherJs : DeletePairAssignmentDocumentCommandDispatcher,
    RequestTribeIdSyntax, RequestPairAssignmentDocumentIdSyntax, JsonSendToResponseSyntax, EndpointHandlerSyntax {
    @JsName("performDeletePairAssignmentDocumentCommand")
    val performDeletePairAssignmentDocumentCommand
        get() = endpointHandler(sendSuccess("Pair Assignments")) {
            DeletePairAssignmentDocumentCommand(tribeId(), pairAssignmentDocumentId())
                .perform()
        }

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

}
