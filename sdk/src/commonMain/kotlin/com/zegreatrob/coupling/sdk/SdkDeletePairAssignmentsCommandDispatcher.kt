package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.action.pairassignmentdocument.DeletePairAssignmentsCommand
import com.zegreatrob.coupling.action.voidResult
import com.zegreatrob.coupling.sdk.gql.GqlTrait
import com.zegreatrob.coupling.sdk.schema.DeletePairAssignmentsMutation
import com.zegreatrob.coupling.sdk.schema.type.DeletePairAssignmentsInput

interface SdkDeletePairAssignmentsCommandDispatcher :
    DeletePairAssignmentsCommand.Dispatcher,
    GqlTrait {
    override suspend fun perform(command: DeletePairAssignmentsCommand) = with(command) {
        DeletePairAssignmentsMutation(
            DeletePairAssignmentsInput(partyId = partyId, pairAssignmentsId = pairAssignmentDocumentId),
        ).execute().data?.deletePairAssignments
            .let { it == true }
            .voidResult()
    }
}
