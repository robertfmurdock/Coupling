package com.zegreatrob.coupling.sdk.pairassignments

import com.zegreatrob.coupling.json.toJsonElement
import com.zegreatrob.coupling.json.toSavePairAssignmentsInput
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.party.PartyElement
import com.zegreatrob.coupling.repository.pairassignmentdocument.PairAssignmentDocumentSave
import com.zegreatrob.coupling.sdk.GqlSyntax
import com.zegreatrob.coupling.sdk.GraphQueries
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject

interface SdkPairAssignmentDocumentSave : PairAssignmentDocumentSave, GqlSyntax, GraphQueries {
    override suspend fun save(partyPairDocument: PartyElement<PairAssignmentDocument>) {
        performQuery(
            buildJsonObject {
                put("query", mutations.savePairAssignments)
                putJsonObject("variables") {
                    put("input", partyPairDocument.savePairAssignmentsInput())
                }
            }
        )
    }

    private fun PartyElement<PairAssignmentDocument>.savePairAssignmentsInput() =
        toSavePairAssignmentsInput().toJsonElement()
}
