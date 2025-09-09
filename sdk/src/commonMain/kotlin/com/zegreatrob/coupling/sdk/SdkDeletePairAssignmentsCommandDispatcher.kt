package com.zegreatrob.coupling.sdk

import com.example.DeletePairAssignmentsMutation
import com.example.type.DeletePairAssignmentsInput
import com.zegreatrob.coupling.action.pairassignmentdocument.DeletePairAssignmentsCommand
import com.zegreatrob.coupling.action.voidResult
import com.zegreatrob.coupling.sdk.gql.GqlTrait

interface SdkDeletePairAssignmentsCommandDispatcher :
    DeletePairAssignmentsCommand.Dispatcher,
    GqlTrait {
    override suspend fun perform(command: DeletePairAssignmentsCommand) = with(command) {
        apolloMutation(
            DeletePairAssignmentsMutation(
                DeletePairAssignmentsInput(partyId = partyId, pairAssignmentsId = pairAssignmentDocumentId),
            ),
        ).data?.deletePairAssignments
            .let { it == true }
            .voidResult()
    }
}
