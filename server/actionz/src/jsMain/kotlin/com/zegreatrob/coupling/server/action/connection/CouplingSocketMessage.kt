package com.zegreatrob.coupling.server.action.connection

import com.zegreatrob.coupling.model.CouplingConnection
import com.zegreatrob.coupling.model.CouplingSocketMessage
import com.zegreatrob.coupling.model.pairassignmentdocument.PairingSet

fun couplingSocketMessage(connections: List<CouplingConnection>, doc: PairingSet?) = CouplingSocketMessage(
    "Users viewing this page: ${connections.size}",
    connections.map { it.userPlayer }.toSet(),
    doc,
)
