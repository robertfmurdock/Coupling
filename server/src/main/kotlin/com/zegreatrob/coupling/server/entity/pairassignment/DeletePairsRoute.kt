package com.zegreatrob.coupling.server.entity.pairassignment

import com.zegreatrob.coupling.server.action.pairassignmentdocument.DeletePairAssignmentDocumentCommand
import com.zegreatrob.coupling.server.express.ResponseHelpers
import com.zegreatrob.coupling.server.express.route.ExpressDispatchers.command
import com.zegreatrob.coupling.server.express.route.dispatch
import com.zegreatrob.coupling.server.external.express.Request
import com.zegreatrob.coupling.server.external.express.pairAssignmentDocumentId
import com.zegreatrob.coupling.server.external.express.tribeId

private val sendDeleteResults = ResponseHelpers.sendDeleteResults("Pair Assignments")

val deletePairsRoute = dispatch(command, Request::command, { it }, sendDeleteResults)

private fun Request.command() = DeletePairAssignmentDocumentCommand(
    tribeId(),
    pairAssignmentDocumentId()
)
