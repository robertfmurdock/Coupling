package com.zegreatrob.coupling.server.entity.pairassignment

import com.zegreatrob.coupling.json.couplingJsonFormat
import com.zegreatrob.coupling.json.toSerializable
import com.zegreatrob.coupling.model.TribeRecord
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.server.action.pairassignmentdocument.CurrentPairAssignmentDocumentQuery
import com.zegreatrob.coupling.server.graphql.DispatcherProviders.tribeCommand
import com.zegreatrob.coupling.server.graphql.dispatch
import kotlinx.serialization.json.encodeToDynamic
import kotlin.js.Json

val currentPairAssignmentResolve = dispatch(
    tribeCommand,
    { _, _ -> CurrentPairAssignmentDocumentQuery },
    ::toJson
)

private fun toJson(result: TribeRecord<PairAssignmentDocument>?) =
    result?.let { couplingJsonFormat.encodeToDynamic(it.toSerializable()).unsafeCast<Json>() }
