package com.zegreatrob.coupling.server.entity.pairassignment

import com.zegreatrob.coupling.json.toSerializable
import com.zegreatrob.coupling.model.PartyRecord
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.server.action.pairassignmentdocument.PairAssignmentDocumentListQuery
import com.zegreatrob.coupling.server.graphql.DispatcherProviders.partyCommand
import com.zegreatrob.coupling.server.graphql.dispatch

val pairAssignmentListResolve = dispatch(
    partyCommand,
    { _, _ -> PairAssignmentDocumentListQuery },
    ::toSerializable,
)

private fun toSerializable(result: List<PartyRecord<PairAssignmentDocument>>) =
    result.map(PartyRecord<PairAssignmentDocument>::toSerializable)
