package com.zegreatrob.coupling.server.entity.pairassignment

import com.zegreatrob.coupling.json.toJson
import com.zegreatrob.coupling.json.toPairAssignmentDocument
import com.zegreatrob.coupling.model.pairassignmentdocument.TribeIdPairAssignmentDocument
import com.zegreatrob.coupling.server.JsonSendToResponseSyntax
import com.zegreatrob.coupling.server.action.pairassignmentdocument.SavePairAssignmentDocumentCommand
import com.zegreatrob.coupling.server.action.pairassignmentdocument.SavePairAssignmentDocumentCommandDispatcher
import com.zegreatrob.coupling.server.entity.tribe.RequestTribeIdSyntax
import com.zegreatrob.coupling.server.entity.tribe.ScopeSyntax
import com.zegreatrob.coupling.server.external.express.Request
import com.zegreatrob.coupling.server.external.express.Response
import com.zegreatrob.coupling.server.external.express.jsonBody
import kotlinx.coroutines.promise

interface SavePairAssignmentDocumentCommandDispatcherJs : SavePairAssignmentDocumentCommandDispatcher, ScopeSyntax,
    RequestTribeIdSyntax, JsonSendToResponseSyntax {

    @JsName("performSavePairAssignmentDocumentCommand")
    fun performSavePairAssignmentDocumentCommand(request: Request, response: Response) = scope.promise {
        SavePairAssignmentDocumentCommand(
            TribeIdPairAssignmentDocument(
                request.tribeId(),
                request.jsonBody().toPairAssignmentDocument()
            )
        )
            .perform()
            .document
            .toJson()
            .sendTo(response)
    }
}
