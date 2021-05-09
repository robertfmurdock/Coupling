package com.zegreatrob.coupling.server.entity.pairassignment

import com.zegreatrob.coupling.json.toJson
import com.zegreatrob.coupling.model.TribeRecord
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pairassignmentdocument.document
import com.zegreatrob.coupling.server.action.pairassignmentdocument.PairAssignmentDocumentListQuery
import com.zegreatrob.coupling.server.graphql.DispatcherProviders.tribeCommand
import com.zegreatrob.coupling.server.graphql.dispatch

val pairAssignmentListResolve = dispatch(
    tribeCommand,
    { _,_ -> PairAssignmentDocumentListQuery },
    ::toJson
)

private fun toJson(result: List<TribeRecord<PairAssignmentDocument>>) = result
    .map { it.toJson().add(it.data.document.toJson()) }
    .toTypedArray()
