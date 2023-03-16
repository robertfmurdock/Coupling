package com.zegreatrob.coupling.server.action.connection

import com.zegreatrob.coupling.model.CouplingConnection
import com.zegreatrob.coupling.model.CouplingSocketMessage
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument

fun couplingSocketMessage(connections: List<CouplingConnection>, doc: PairAssignmentDocument?) =
    CouplingSocketMessage(
        "Users viewing this page: ${connections.size}",
        connections.map { it.userPlayer }.toSet(),
        doc,
    )
