package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.action.VoidResult
import com.zegreatrob.coupling.action.pairassignmentdocument.SavePairAssignmentsCommand
import com.zegreatrob.coupling.json.toJsonElement
import com.zegreatrob.coupling.json.toSavePairAssignmentsInput
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.party.PartyElement
import com.zegreatrob.coupling.model.party.with
import com.zegreatrob.coupling.repository.pairassignmentdocument.PairAssignmentDocumentSave
import com.zegreatrob.coupling.sdk.gql.GqlTrait
import com.zegreatrob.coupling.sdk.gql.Mutation
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject

interface SdkPairAssignmentDocumentSave :
    PairAssignmentDocumentSave,
    GqlTrait {
    override suspend fun save(partyPairDocument: PartyElement<PairAssignmentDocument>) {
        performQuery(
            buildJsonObject {
                put("query", Mutation.savePairAssignments)
                putJsonObject("variables") {
                    put("input", partyPairDocument.savePairAssignmentsInput())
                }
            },
        )
    }
}

private fun PartyElement<PairAssignmentDocument>.savePairAssignmentsInput() = toSavePairAssignmentsInput().toJsonElement()

interface SdkSavePairAssignmentsCommandDispatcher :
    SavePairAssignmentsCommand.Dispatcher,
    GqlTrait {

    override suspend fun perform(command: SavePairAssignmentsCommand) = with(command) {
        performQuery(
            buildJsonObject {
                put("query", Mutation.savePairAssignments)
                putJsonObject("variables") {
                    put("input", partyId.with(pairAssignments).savePairAssignmentsInput())
                }
            },
        )
        VoidResult.Accepted
    }
}
