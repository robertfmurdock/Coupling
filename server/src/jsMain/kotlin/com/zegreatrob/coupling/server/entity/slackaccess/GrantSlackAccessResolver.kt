package com.zegreatrob.coupling.server.entity.slackaccess

import com.zegreatrob.coupling.action.GrantSlackAccessCommand
import com.zegreatrob.coupling.action.VoidResult
import com.zegreatrob.coupling.action.fire
import com.zegreatrob.coupling.json.GrantSlackAccessInput
import com.zegreatrob.coupling.server.entity.toJson
import com.zegreatrob.coupling.server.external.graphql.Resolver
import com.zegreatrob.coupling.server.graphql.DispatcherProviders
import com.zegreatrob.coupling.server.graphql.dispatchAction
import kotlinx.serialization.json.JsonNull

val grantSlackAccessResolver: Resolver = dispatchAction(
    DispatcherProviders.command(),
    fireCommand = { _: JsonNull, input: GrantSlackAccessInput -> fire(input.command()) },
    toSerializable = { result: VoidResult -> result.toJson() },
)

private fun GrantSlackAccessInput.command() = GrantSlackAccessCommand(code, state)
