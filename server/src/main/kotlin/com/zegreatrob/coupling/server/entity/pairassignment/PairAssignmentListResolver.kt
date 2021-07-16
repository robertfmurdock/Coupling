package com.zegreatrob.coupling.server.entity.pairassignment

import com.zegreatrob.coupling.json.toSerializable
import com.zegreatrob.coupling.model.TribeRecord
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.server.action.pairassignmentdocument.PairAssignmentDocumentListQuery
import com.zegreatrob.coupling.server.graphql.DispatcherProviders.tribeCommand
import com.zegreatrob.coupling.server.graphql.dispatch

val pairAssignmentListResolve = dispatch(tribeCommand, { _, _ -> PairAssignmentDocumentListQuery }, ::toSerializable)

private fun toSerializable(result: List<TribeRecord<PairAssignmentDocument>>) =
    result.map(TribeRecord<PairAssignmentDocument>::toSerializable)
