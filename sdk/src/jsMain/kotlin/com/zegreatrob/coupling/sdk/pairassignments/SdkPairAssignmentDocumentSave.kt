package com.zegreatrob.coupling.sdk.pairassignments

import com.soywiz.klock.js.toDate
import com.zegreatrob.coupling.json.toJson
import com.zegreatrob.coupling.model.pairassignmentdocument.TribeIdPairAssignmentDocument
import com.zegreatrob.coupling.repository.pairassignmentdocument.PairAssignmentDocumentSave
import com.zegreatrob.coupling.sdk.GqlSyntax
import com.zegreatrob.coupling.sdk.Mutations
import kotlin.js.json

interface SdkPairAssignmentDocumentSave : PairAssignmentDocumentSave, GqlSyntax {
    override suspend fun save(tribeIdPairAssignmentDocument: TribeIdPairAssignmentDocument) {
        performQuery(
            json(
                "query" to Mutations.savePairAssignments,
                "variables" to json("input" to tribeIdPairAssignmentDocument.savePairAssignmentsInput())
            )
        )
    }

    private fun TribeIdPairAssignmentDocument.savePairAssignmentsInput() = json("tribeId" to id.value).add(
        json(
            "pairAssignmentsId" to element.id.value,
            "date" to element.date.toDate().toISOString(),
            "pairs" to element.pairs.map {
                json(
                    "players" to it.players
                        .map { player -> player.toJson() }
                        .toTypedArray(),
                    "pins" to it.pins.toJson()
                )
            }
                .toTypedArray()
        )
    )
}