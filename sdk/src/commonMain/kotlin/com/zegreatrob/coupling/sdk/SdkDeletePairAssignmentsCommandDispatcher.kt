package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.action.CommandResult
import com.zegreatrob.coupling.action.pairassignmentdocument.DeletePairAssignmentsCommand
import com.zegreatrob.coupling.action.voidResult
import com.zegreatrob.coupling.json.at
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocumentId
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.sdk.gql.GqlTrait
import com.zegreatrob.coupling.sdk.gql.Mutation
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject

interface SdkDeletePairAssignmentsCommandDispatcher :
    DeletePairAssignmentsCommand.Dispatcher,
    GqlTrait {
    override suspend fun perform(command: DeletePairAssignmentsCommand) = with(command) {
        performQuery(options(partyId, pairAssignmentDocumentId))
            .at("/data/deletePairAssignments")
            ?.jsonPrimitive
            ?.booleanOrNull
            ?.voidResult()
            ?: CommandResult.Unauthorized
    }
}

private fun options(partyId: PartyId, pairAssignmentDocumentId: PairAssignmentDocumentId) = buildJsonObject {
    put("query", JsonPrimitive(Mutation.deletePairAssignments))
    putJsonObject("variables") {
        putJsonObject("input") {
            put("partyId", partyId.value)
            put("pairAssignmentsId", pairAssignmentDocumentId.value)
        }
    }
}
