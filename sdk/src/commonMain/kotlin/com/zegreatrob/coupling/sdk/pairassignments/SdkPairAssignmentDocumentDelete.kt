package com.zegreatrob.coupling.sdk.pairassignments

import com.zegreatrob.coupling.json.at
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocumentId
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.repository.pairassignmentdocument.PairAssignmentDocumentDelete
import com.zegreatrob.coupling.sdk.GqlSyntax
import com.zegreatrob.coupling.sdk.GraphQueries
import kotlinx.serialization.json.*

interface SdkPairAssignmentDocumentDelete : PairAssignmentDocumentDelete, GqlSyntax, GraphQueries {
    override suspend fun delete(
        tribeId: PartyId,
        pairAssignmentDocumentId: PairAssignmentDocumentId
    ): Boolean = performQuery(options(tribeId, pairAssignmentDocumentId))
        .at("/data/deletePairAssignments")
        ?.jsonPrimitive
        ?.booleanOrNull
        ?: false

    private fun options(tribeId: PartyId, pairAssignmentDocumentId: PairAssignmentDocumentId) = buildJsonObject {
        put("query", JsonPrimitive(mutations.deletePairAssignments))
        putJsonObject("variables") {
            putJsonObject("input") {
                put("tribeId", tribeId.value)
                put("pairAssignmentsId", pairAssignmentDocumentId.value)
            }
        }
    }

}