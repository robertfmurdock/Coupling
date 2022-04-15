package com.zegreatrob.coupling.server.entity.pairassignment

import com.zegreatrob.coupling.json.SavePairAssignmentsInput
import com.zegreatrob.coupling.json.toModel
import com.zegreatrob.coupling.action.pairassignmentdocument.SavePairAssignmentDocumentCommand
import com.zegreatrob.coupling.server.graphql.DispatcherProviders.tribeCommand
import com.zegreatrob.coupling.server.graphql.dispatch

val savePairsResolver = dispatch(
    tribeCommand,
    { _, args: SavePairAssignmentsInput -> SavePairAssignmentDocumentCommand(args.toModel()) },
    { true }
)
