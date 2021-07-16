package com.zegreatrob.coupling.server.entity.pairassignment

import com.zegreatrob.coupling.json.SavePairAssignmentsInput
import com.zegreatrob.coupling.json.couplingJsonFormat
import com.zegreatrob.coupling.json.toModel
import com.zegreatrob.coupling.json.toSerializable
import com.zegreatrob.coupling.model.pairassignmentdocument.TribeIdPairAssignmentDocument
import com.zegreatrob.coupling.model.pairassignmentdocument.document
import com.zegreatrob.coupling.server.action.pairassignmentdocument.SavePairAssignmentDocumentCommand
import com.zegreatrob.coupling.server.graphql.DispatcherProviders.tribeCommand
import com.zegreatrob.coupling.server.graphql.dispatch
import com.zegreatrob.minjson.at
import kotlinx.serialization.json.decodeFromDynamic
import kotlin.js.Json

val savePairsResolver = dispatch(
    tribeCommand,
    { _, args -> args.at<Json>("input").toModel().let(::SavePairAssignmentDocumentCommand) },
    ::toSerializable
)

private fun Json?.toModel() = couplingJsonFormat.decodeFromDynamic<SavePairAssignmentsInput>(this).toModel()

private fun toSerializable(result: TribeIdPairAssignmentDocument) = result.document.toSerializable()

