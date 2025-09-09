package com.zegreatrob.coupling.sdk

import com.example.ClearContributionsMutation
import com.example.type.ClearContributionsInput
import com.zegreatrob.coupling.action.VoidResult
import com.zegreatrob.coupling.action.party.ClearContributionsCommand
import com.zegreatrob.coupling.sdk.gql.GqlTrait

interface SdkClearContributionCommandDispatcher :
    ClearContributionsCommand.Dispatcher,
    GqlTrait {
    override suspend fun perform(command: ClearContributionsCommand): VoidResult {
        apolloMutation(ClearContributionsMutation(command.clearContributionInput()))
        return VoidResult.Accepted
    }
}

private fun ClearContributionsCommand.clearContributionInput() = ClearContributionsInput(partyId = partyId)
