package com.zegreatrob.coupling.sdk.pairassignments

import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocumentId
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.repository.pairassignmentdocument.PairAssignmentDocumentDelete
import com.zegreatrob.coupling.sdk.GqlSyntax
import com.zegreatrob.coupling.sdk.Mutations
import com.zegreatrob.minjson.at
import kotlin.js.Json
import kotlin.js.json

interface SdkPairAssignmentDocumentDelete : PairAssignmentDocumentDelete, GqlSyntax {
    override suspend fun delete(
        tribeId: TribeId,
        pairAssignmentDocumentId: PairAssignmentDocumentId
    ): Boolean = performQuery(
        json(
            "query" to Mutations.deletePairAssignments,
            "variables" to json("input" to deletePairAssignmentsInput(tribeId, pairAssignmentDocumentId))
        )
    ).unsafeCast<Json>()
        .at("/data/data/deletePairAssignments") ?: false

    private fun deletePairAssignmentsInput(tribeId: TribeId, pairAssignmentDocumentId: PairAssignmentDocumentId) = json(
        "tribeId" to tribeId.value,
        "pairAssignmentsId" to pairAssignmentDocumentId.value
    )
}