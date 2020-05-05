package com.zegreatrob.coupling.server.entity.pairassignment

import com.zegreatrob.coupling.json.toJson
import com.zegreatrob.coupling.json.toPairAssignmentDocument
import com.zegreatrob.coupling.model.pairassignmentdocument.TribeIdPairAssignmentDocument
import com.zegreatrob.coupling.model.pairassignmentdocument.document
import com.zegreatrob.coupling.model.tribe.with
import com.zegreatrob.coupling.server.action.pairassignmentdocument.SavePairAssignmentDocumentCommand
import com.zegreatrob.coupling.server.external.express.Request
import com.zegreatrob.coupling.server.external.express.jsonBody
import com.zegreatrob.coupling.server.external.express.tribeId
import com.zegreatrob.coupling.server.route.dispatchCommand

val savePairsRoute = dispatchCommand(Request::command, { it.perform() }, ::toJson)

private fun Request.command() = SavePairAssignmentDocumentCommand(
    tribeId().with(
        jsonBody().toPairAssignmentDocument()
    )
)

private fun toJson(result: TribeIdPairAssignmentDocument) = result.document.toJson()
