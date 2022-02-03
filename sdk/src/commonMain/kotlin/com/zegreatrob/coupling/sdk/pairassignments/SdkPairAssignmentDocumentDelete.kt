package com.zegreatrob.coupling.sdk.pairassignments

import com.zegreatrob.coupling.json.at
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocumentId
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.repository.pairassignmentdocument.PairAssignmentDocumentDelete
import com.zegreatrob.coupling.sdk.GqlSyntax
import com.zegreatrob.coupling.sdk.Mutations
import kotlinx.serialization.json.*

interface SdkPairAssignmentDocumentDelete : PairAssignmentDocumentDelete, GqlSyntax {
    override suspend fun delete(
        tribeId: TribeId,
        pairAssignmentDocumentId: PairAssignmentDocumentId
    ): Boolean = performQuery(options(tribeId, pairAssignmentDocumentId))
        .at("/data/deletePairAssignments")
        ?.jsonPrimitive
        ?.booleanOrNull
        ?: false

    private fun options(tribeId: TribeId, pairAssignmentDocumentId: PairAssignmentDocumentId) = buildJsonObject {
        put("query", JsonPrimitive(Mutations.deletePairAssignments))
        putJsonObject("variables") {
            putJsonObject("input") {
                put("tribeId", tribeId.value)
                put("pairAssignmentsId", pairAssignmentDocumentId.value)
            }
        }
    }

}