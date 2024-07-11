package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.action.VoidResult
import com.zegreatrob.coupling.action.party.ClearContributionsCommand
import com.zegreatrob.coupling.json.ClearContributionsInput
import com.zegreatrob.coupling.sdk.gql.GqlTrait
import com.zegreatrob.coupling.sdk.gql.Mutation
import com.zegreatrob.coupling.sdk.gql.doQuery

interface SdkClearContributionCommandDispatcher :
    ClearContributionsCommand.Dispatcher,
    GqlTrait {
    override suspend fun perform(command: ClearContributionsCommand): VoidResult {
        doQuery(Mutation.clearContributions, command.clearContributionInput())
        return VoidResult.Accepted
    }
}

private fun ClearContributionsCommand.clearContributionInput() = ClearContributionsInput(partyId = partyId.value)
