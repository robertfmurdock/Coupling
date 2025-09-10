package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.action.CommandResult
import com.zegreatrob.coupling.action.GrantSlackAccessCommand
import com.zegreatrob.coupling.action.VoidResult
import com.zegreatrob.coupling.sdk.gql.GqlTrait
import com.zegreatrob.coupling.sdk.schema.GrantSlackAccessMutation
import com.zegreatrob.coupling.sdk.schema.type.GrantSlackAccessInput

interface SdkGrantSlackAccess :
    GrantSlackAccessCommand.Dispatcher,
    GqlTrait {
    override suspend fun perform(command: GrantSlackAccessCommand): VoidResult = GrantSlackAccessMutation(command.grantSlackAccessInput()).execute()
        .data?.grantSlackAccess?.toVoidResult() ?: CommandResult.Unauthorized
}

internal fun GrantSlackAccessCommand.grantSlackAccessInput() = GrantSlackAccessInput(
    code = code,
    state = state,
)

fun Boolean?.toVoidResult() = when (this) {
    true -> VoidResult.Accepted
    false -> VoidResult.Rejected
    null -> CommandResult.Unauthorized
}
