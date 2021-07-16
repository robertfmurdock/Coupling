package com.zegreatrob.coupling.server.entity.pairassignment

import com.zegreatrob.coupling.json.SavePairAssignmentsInput
import com.zegreatrob.coupling.json.toModel
import com.zegreatrob.coupling.json.toSerializable
import com.zegreatrob.coupling.model.pairassignmentdocument.TribeIdPairAssignmentDocument
import com.zegreatrob.coupling.model.pairassignmentdocument.document
import com.zegreatrob.coupling.server.action.pairassignmentdocument.SavePairAssignmentDocumentCommand
import com.zegreatrob.coupling.server.graphql.DispatcherProviders.tribeCommand
import com.zegreatrob.coupling.server.graphql.dispatch

val savePairsResolver = dispatch(
    tribeCommand,
    { _, args: SavePairAssignmentsInput -> SavePairAssignmentDocumentCommand(args.toModel()) },
    ::toSerializable
)

private fun toSerializable(result: TribeIdPairAssignmentDocument) = result.document.toSerializable()
