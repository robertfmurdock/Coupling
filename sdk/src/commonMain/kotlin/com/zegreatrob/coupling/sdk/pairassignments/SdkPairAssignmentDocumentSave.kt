package com.zegreatrob.coupling.sdk.pairassignments

import com.zegreatrob.coupling.json.couplingJsonFormat
import com.zegreatrob.coupling.json.toSavePairAssignmentsInput
import com.zegreatrob.coupling.model.pairassignmentdocument.TribeIdPairAssignmentDocument
import com.zegreatrob.coupling.repository.pairassignmentdocument.PairAssignmentDocumentSave
import com.zegreatrob.coupling.sdk.GqlSyntax
import com.zegreatrob.coupling.sdk.Mutations
import kotlinx.serialization.json.encodeToDynamic
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

    private fun TribeIdPairAssignmentDocument.savePairAssignmentsInput() = couplingJsonFormat.encodeToDynamic(
        toSavePairAssignmentsInput()
    )

}