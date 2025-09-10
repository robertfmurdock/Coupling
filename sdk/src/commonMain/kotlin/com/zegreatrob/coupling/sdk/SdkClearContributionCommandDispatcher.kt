package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.action.VoidResult
import com.zegreatrob.coupling.action.party.ClearContributionsCommand
import com.zegreatrob.coupling.sdk.gql.GqlTrait
import com.zegreatrob.coupling.sdk.schema.ClearContributionsMutation
import com.zegreatrob.coupling.sdk.schema.type.ClearContributionsInput

interface SdkClearContributionCommandDispatcher :
    ClearContributionsCommand.Dispatcher,
    GqlTrait {
    override suspend fun perform(command: ClearContributionsCommand): VoidResult {
        ClearContributionsMutation(command.clearContributionInput()).execute()
        return VoidResult.Accepted
    }
}

private fun ClearContributionsCommand.clearContributionInput() = ClearContributionsInput(partyId = partyId)
