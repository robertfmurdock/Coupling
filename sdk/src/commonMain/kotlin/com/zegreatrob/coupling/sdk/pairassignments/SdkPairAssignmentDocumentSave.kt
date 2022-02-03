package com.zegreatrob.coupling.sdk.pairassignments

import com.zegreatrob.coupling.json.toJsonElement
import com.zegreatrob.coupling.json.toSavePairAssignmentsInput
import com.zegreatrob.coupling.model.pairassignmentdocument.TribeIdPairAssignmentDocument
import com.zegreatrob.coupling.repository.pairassignmentdocument.PairAssignmentDocumentSave
import com.zegreatrob.coupling.sdk.GqlSyntax
import com.zegreatrob.coupling.sdk.Mutations
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject

interface SdkPairAssignmentDocumentSave : PairAssignmentDocumentSave, GqlSyntax {
    override suspend fun save(tribeIdPairAssignmentDocument: TribeIdPairAssignmentDocument) {
        performQuery(
            buildJsonObject {
                put("query", Mutations.savePairAssignments)
                putJsonObject("variables") {
                    put("input", tribeIdPairAssignmentDocument.savePairAssignmentsInput())
                }
            }
        )
    }

    private fun TribeIdPairAssignmentDocument.savePairAssignmentsInput() = toSavePairAssignmentsInput().toJsonElement()

}