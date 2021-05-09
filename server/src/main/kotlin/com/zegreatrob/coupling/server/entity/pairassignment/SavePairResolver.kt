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
import kotlin.js.Json

val savePairsResolver = dispatch(
    tribeCommand,
    { _, args ->
        args.savePairAssignmentsInput()
            .toPairAssignmentDocument()
            .let(::SavePairAssignmentDocumentCommand)
    },
    ::toJson
)

private fun Json.toPairAssignmentDocument() = PairAssignmentDocument(
    id = PairAssignmentDocumentId(this["pairAssignmentsId"].unsafeCast<String>()),
    date = this["date"].let(::toDate).toDateTime(),
    pairs = this["pairs"].unsafeCast<Array<Any>?>()?.map(::pairFromJson) ?: emptyList()
)

private fun Json.savePairAssignmentsInput() = this["input"].unsafeCast<Json>()


private fun toJson(result: TribeIdPairAssignmentDocument) = result.document.toJson()
