package com.zegreatrob.coupling.sdk.pairassignments

import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocumentId
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.repository.pairassignmentdocument.PairAssignmentDocumentDelete
import com.zegreatrob.coupling.sdk.AxiosSyntax
import com.zegreatrob.coupling.sdk.GqlSyntax
import com.zegreatrob.coupling.sdk.Mutations
import kotlin.js.json

interface SdkPairAssignmentDocumentDelete : PairAssignmentDocumentDelete, GqlSyntax {
    override suspend fun delete(
        tribeId: TribeId,
        pairAssignmentDocumentId: PairAssignmentDocumentId
    ): Boolean {
        return performQuery(
            json(
                "query" to Mutations.deletePairAssignments,
                "variables" to json("input" to deletePairAssignmentsInput(tribeId, pairAssignmentDocumentId))
            )
        )
            .data.data.deletePairAssignments
            .unsafeCast<Boolean?>() ?: false
    }

    private fun deletePairAssignmentsInput(tribeId: TribeId, pairAssignmentDocumentId: PairAssignmentDocumentId) = json(
        "tribeId" to tribeId.value,
        "pairAssignmentsId" to pairAssignmentDocumentId.value
    )
}