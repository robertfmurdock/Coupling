package com.zegreatrob.coupling.server.entity.pairassignment

import com.zegreatrob.coupling.json.couplingJsonFormat
import com.zegreatrob.coupling.json.toSerializable
import com.zegreatrob.coupling.model.TribeRecord
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.server.action.pairassignmentdocument.PairAssignmentDocumentListQuery
import com.zegreatrob.coupling.server.graphql.DispatcherProviders.tribeCommand
import com.zegreatrob.coupling.server.graphql.dispatch
import kotlinx.serialization.json.encodeToDynamic
import kotlin.js.Json

val pairAssignmentListResolve = dispatch(tribeCommand, { _, _ -> PairAssignmentDocumentListQuery }, ::toJson)

private fun toJson(result: List<TribeRecord<PairAssignmentDocument>>) = couplingJsonFormat.encodeToDynamic(
    result.map(TribeRecord<PairAssignmentDocument>::toSerializable)
).unsafeCast<Array<Json>>()
