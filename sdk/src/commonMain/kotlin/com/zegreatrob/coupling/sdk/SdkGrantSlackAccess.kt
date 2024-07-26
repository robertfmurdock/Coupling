package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.action.CommandResult
import com.zegreatrob.coupling.action.GrantSlackAccessCommand
import com.zegreatrob.coupling.action.VoidResult
import com.zegreatrob.coupling.json.GqlGrantSlackAccessInput
import com.zegreatrob.coupling.sdk.gql.GqlTrait
import com.zegreatrob.coupling.sdk.gql.Mutation
import com.zegreatrob.coupling.sdk.gql.doQuery

interface SdkGrantSlackAccess :
    GrantSlackAccessCommand.Dispatcher,
    GqlTrait {
    override suspend fun perform(command: GrantSlackAccessCommand): VoidResult = doQuery(
        mutation = Mutation.grantSlackAccess,
        input = command.grantSlackAccessInput(),
        resultName = "grantSlackAccess",
        toOutput = Boolean?::toVoidResult,
    ) ?: CommandResult.Unauthorized
}

fun GrantSlackAccessCommand.grantSlackAccessInput() = GqlGrantSlackAccessInput(
    code = code,
    state = state,
)

fun Boolean?.toVoidResult() = when (this) {
    true -> VoidResult.Accepted
    false -> VoidResult.Rejected
    null -> CommandResult.Unauthorized
}
