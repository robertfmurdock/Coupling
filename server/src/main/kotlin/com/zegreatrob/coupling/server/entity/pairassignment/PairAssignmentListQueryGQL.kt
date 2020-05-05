package com.zegreatrob.coupling.server.entity.pairassignment

import com.zegreatrob.coupling.json.toJson
import com.zegreatrob.coupling.model.TribeRecord
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pairassignmentdocument.document
import com.zegreatrob.coupling.server.action.pairassignmentdocument.PairAssignmentDocumentListQuery
import com.zegreatrob.coupling.server.entity.dispatchTribeCommand

val pairAssignmentQueryRoute = dispatchTribeCommand({ PairAssignmentDocumentListQuery }, { it.perform() }, ::toJson)

private fun toJson(perform: List<TribeRecord<PairAssignmentDocument>>) = perform
    .map { it.toJson().add(it.data.document.toJson()) }
    .toTypedArray()
