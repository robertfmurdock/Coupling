package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.action.pairassignmentdocument.DeletePairAssignmentsCommand
import com.zegreatrob.coupling.action.voidResult
import com.zegreatrob.coupling.json.GqlDeletePairAssignmentsInput
import com.zegreatrob.coupling.sdk.gql.GqlTrait
import com.zegreatrob.coupling.sdk.gql.Mutation
import com.zegreatrob.coupling.sdk.gql.doQuery

interface SdkDeletePairAssignmentsCommandDispatcher :
    DeletePairAssignmentsCommand.Dispatcher,
    GqlTrait {
    override suspend fun perform(command: DeletePairAssignmentsCommand) = with(command) {
        doQuery(
            Mutation.deletePairAssignments,
            GqlDeletePairAssignmentsInput(partyId.value, pairAssignmentDocumentId.value.toString()),
            "deletePairAssignments",
        ) { it: Boolean? -> it }
            .let { it == true }
            .voidResult()
    }
}
