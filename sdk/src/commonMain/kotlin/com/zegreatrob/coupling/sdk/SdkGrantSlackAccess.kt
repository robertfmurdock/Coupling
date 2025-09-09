package com.zegreatrob.coupling.sdk

import com.example.GrantSlackAccessMutation
import com.example.type.GrantSlackAccessInput
import com.zegreatrob.coupling.action.CommandResult
import com.zegreatrob.coupling.action.GrantSlackAccessCommand
import com.zegreatrob.coupling.action.VoidResult
import com.zegreatrob.coupling.sdk.gql.GqlTrait

interface SdkGrantSlackAccess :
    GrantSlackAccessCommand.Dispatcher,
    GqlTrait {
    override suspend fun perform(command: GrantSlackAccessCommand): VoidResult = apolloMutation(GrantSlackAccessMutation(command.grantSlackAccessInput()))
        .data?.grantSlackAccess?.toVoidResult() ?: CommandResult.Unauthorized
}

fun GrantSlackAccessCommand.grantSlackAccessInput() = GrantSlackAccessInput(
    code = code,
    state = state,
)

fun Boolean?.toVoidResult() = when (this) {
    true -> VoidResult.Accepted
    false -> VoidResult.Rejected
    null -> CommandResult.Unauthorized
}
