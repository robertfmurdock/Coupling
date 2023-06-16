package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.action.CommandResult
import com.zegreatrob.coupling.action.GrantSlackAccessCommand
import com.zegreatrob.coupling.action.VoidResult
import com.zegreatrob.coupling.json.GrantSlackAccessInput
import com.zegreatrob.coupling.sdk.gql.GqlSyntax
import com.zegreatrob.coupling.sdk.gql.Mutation
import com.zegreatrob.coupling.sdk.gql.doQuery
import kotlinx.serialization.json.JsonElement

interface SdkGrantSlackAccess : GrantSlackAccessCommand.Dispatcher, GqlSyntax {
    override suspend fun perform(command: GrantSlackAccessCommand): VoidResult = doQuery(
        mutation = Mutation.grantSlackAccess,
        input = command.grantSlackAccessInput(),
        resultName = "grantSlackAccess",
        toOutput = { _: JsonElement -> VoidResult.Accepted },
    ) ?: CommandResult.Unauthorized
}

fun GrantSlackAccessCommand.grantSlackAccessInput() = GrantSlackAccessInput(
    code = code,
    state = state,
)
