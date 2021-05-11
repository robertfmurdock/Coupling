package com.zegreatrob.coupling.server.entity.pairassignment

import com.soywiz.klock.js.toDateTime
import com.zegreatrob.coupling.json.pairFromJson
import com.zegreatrob.coupling.json.toDate
import com.zegreatrob.coupling.json.toJson
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocumentId
import com.zegreatrob.coupling.model.pairassignmentdocument.TribeIdPairAssignmentDocument
import com.zegreatrob.coupling.model.pairassignmentdocument.document
import com.zegreatrob.coupling.server.action.pairassignmentdocument.SavePairAssignmentDocumentCommand
import com.zegreatrob.coupling.server.graphql.DispatcherProviders.tribeCommand
import com.zegreatrob.coupling.server.graphql.dispatch
import com.zegreatrob.minjson.at
import kotlin.js.Json

val savePairsResolver = dispatch(
    tribeCommand,
    { _, args -> args.toPairAssignmentDocument().let(::SavePairAssignmentDocumentCommand) },
    ::toJson
)

private fun Json.toPairAssignmentDocument() = PairAssignmentDocument(
    id = PairAssignmentDocumentId(at("/input/pairAssignmentsId")!!),
    date = at<String>("/input/date").let(::toDate).toDateTime(),
    pairs = at<Array<Json>>("/input/pairs")?.map(::pairFromJson)!!
)

private fun toJson(result: TribeIdPairAssignmentDocument) = result.document.toJson()
