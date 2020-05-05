package com.zegreatrob.coupling.server.pairassignments

import com.zegreatrob.coupling.server.ResponseHelpers
import com.zegreatrob.coupling.server.action.pairassignmentdocument.DeletePairAssignmentDocumentCommand
import com.zegreatrob.coupling.server.action.pairassignmentdocument.DeletePairAssignmentDocumentCommandDispatcher
import com.zegreatrob.coupling.server.external.express.Request
import com.zegreatrob.coupling.server.external.express.pairAssignmentDocumentId
import com.zegreatrob.coupling.server.external.express.tribeId
import com.zegreatrob.coupling.server.route.dispatchCommand

private val sendDeleteResults = ResponseHelpers.sendDeleteResults("Pair Assignments")

val deletePairsRoute = dispatchCommand { endpointHandler(sendDeleteResults, ::deleteAssignments) }

private suspend fun DeletePairAssignmentDocumentCommandDispatcher.deleteAssignments(request: Request) =
    request.command().perform()

private fun Request.command() = DeletePairAssignmentDocumentCommand(tribeId(), pairAssignmentDocumentId())
