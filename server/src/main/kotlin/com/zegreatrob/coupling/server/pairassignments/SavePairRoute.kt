package com.zegreatrob.coupling.server.pairassignments

import com.zegreatrob.coupling.json.toJson
import com.zegreatrob.coupling.json.toPairAssignmentDocument
import com.zegreatrob.coupling.model.pairassignmentdocument.document
import com.zegreatrob.coupling.model.tribe.with
import com.zegreatrob.coupling.server.action.pairassignmentdocument.SavePairAssignmentDocumentCommand
import com.zegreatrob.coupling.server.action.pairassignmentdocument.SavePairAssignmentDocumentCommandDispatcher
import com.zegreatrob.coupling.server.external.express.*
import com.zegreatrob.coupling.server.route.dispatch

val savePairsRoute = dispatch { endpointHandler(Response::sendSuccessful, ::handleSave) }

private suspend fun SavePairAssignmentDocumentCommandDispatcher.handleSave(request: Request) =
    request.savePairAssignmentDocumentCommand()
        .perform()
        .document
        .toJson()

private fun Request.savePairAssignmentDocumentCommand() = SavePairAssignmentDocumentCommand(
    tribeId().with(
        jsonBody().toPairAssignmentDocument()
    )
)