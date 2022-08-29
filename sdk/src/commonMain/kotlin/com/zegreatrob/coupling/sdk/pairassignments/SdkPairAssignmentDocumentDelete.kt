package com.zegreatrob.coupling.sdk.pairassignments

import com.zegreatrob.coupling.json.at
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocumentId
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.repository.pairassignmentdocument.PairAssignmentDocumentDelete
import com.zegreatrob.coupling.sdk.GqlSyntax
import com.zegreatrob.coupling.sdk.GraphQueries
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject

interface SdkPairAssignmentDocumentDelete : PairAssignmentDocumentDelete, GqlSyntax, GraphQueries {
    override suspend fun deleteIt(
        partyId: PartyId,
        pairAssignmentDocumentId: PairAssignmentDocumentId
    ): Boolean = performQuery(options(partyId, pairAssignmentDocumentId))
        .at("/data/deletePairAssignments")
        ?.jsonPrimitive
        ?.booleanOrNull
        ?: false

    private fun options(partyId: PartyId, pairAssignmentDocumentId: PairAssignmentDocumentId) = buildJsonObject {
        put("query", JsonPrimitive(mutations.deletePairAssignments))
        putJsonObject("variables") {
            putJsonObject("input") {
                put("partyId", partyId.value)
                put("pairAssignmentsId", pairAssignmentDocumentId.value)
            }
        }
    }
}
