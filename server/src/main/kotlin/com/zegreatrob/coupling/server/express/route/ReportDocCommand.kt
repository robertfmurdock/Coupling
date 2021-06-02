package com.zegreatrob.coupling.server.express.route

import com.zegreatrob.coupling.model.CouplingSocketMessage
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.testmints.action.async.SimpleSuspendAction

data class ReportDocCommand(val tribeId: TribeId, val doc: PairAssignmentDocument?) :
    SimpleSuspendAction<ReportDocCommandDispatcher, CouplingSocketMessage> {
    override val performFunc = link(ReportDocCommandDispatcher::perform)
}

interface ReportDocCommandDispatcher : CouplingConnectionGetSyntax {
    suspend fun perform(command: ReportDocCommand) = with(command) {
        val info = tribeId.loadConnections()
        couplingSocketMessage(info, doc)
    }
}