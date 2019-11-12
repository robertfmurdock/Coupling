package com.zegreatrob.coupling.server.entity.pairassignment

import com.zegreatrob.coupling.server.JsonSendToResponseSyntax
import com.zegreatrob.coupling.server.action.pairassignmentdocument.DeletePairAssignmentDocumentCommand
import com.zegreatrob.coupling.server.action.pairassignmentdocument.DeletePairAssignmentDocumentCommandDispatcher
import com.zegreatrob.coupling.server.entity.tribe.RequestTribeIdSyntax
import com.zegreatrob.coupling.server.entity.tribe.ScopeSyntax
import com.zegreatrob.coupling.server.external.express.Request
import com.zegreatrob.coupling.server.external.express.Response
import kotlinx.coroutines.promise
import kotlin.js.json

interface DeletePairAssignmentDocumentCommandDispatcherJs : DeletePairAssignmentDocumentCommandDispatcher, ScopeSyntax,
    RequestTribeIdSyntax, RequestPairAssignmentDocumentIdSyntax, JsonSendToResponseSyntax {
    @JsName("performDeletePairAssignmentDocumentCommand")
    fun performDeletePairAssignmentDocumentCommand(request: Request, response: Response) = scope.promise {
        val result = DeletePairAssignmentDocumentCommand(request.tribeId(), request.pairAssignmentDocumentId())
            .perform()

        if (result) {
            json("message" to "SUCCESS")
                .sendTo(response)
        } else {
            json("message" to "Pair Assignments could not be deleted because they do not exist.")
                .sendTo(response, 404)
        }
    }
}
