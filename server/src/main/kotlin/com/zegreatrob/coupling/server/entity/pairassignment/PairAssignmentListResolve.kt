package com.zegreatrob.coupling.server.entity.pairassignment

import com.zegreatrob.coupling.json.toJson
import com.zegreatrob.coupling.model.TribeRecord
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pairassignmentdocument.document
import com.zegreatrob.coupling.server.action.pairassignmentdocument.PairAssignmentDocumentListQuery
import com.zegreatrob.coupling.server.graphql.dispatchCommand
import com.zegreatrob.coupling.server.graphql.tribeCommandDispatcher
import kotlin.js.Json

val pairAssignmentListResolve = dispatchCommand(::tribeCommandDispatcher, ::query, ::toJson)

private fun query(it: Json) = PairAssignmentDocumentListQuery

private fun toJson(result: List<TribeRecord<PairAssignmentDocument>>) = result
    .map { it.toJson().add(it.data.document.toJson()) }
    .toTypedArray()
