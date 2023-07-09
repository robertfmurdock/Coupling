package com.zegreatrob.coupling.server.entity.slackaccess

import com.zegreatrob.coupling.action.GrantSlackAccessCommand
import com.zegreatrob.coupling.action.VoidResult
import com.zegreatrob.coupling.json.GrantSlackAccessInput
import com.zegreatrob.coupling.server.entity.toJson
import com.zegreatrob.coupling.server.external.graphql.Resolver
import com.zegreatrob.coupling.server.graphql.DispatcherProviders
import com.zegreatrob.coupling.server.graphql.dispatch
import kotlinx.serialization.json.JsonNull

val grantSlackAccessResolver: Resolver = dispatch(
    DispatcherProviders.command(),
    { _: JsonNull, input: GrantSlackAccessInput -> input.command() },
    VoidResult::toJson,
)

private fun GrantSlackAccessInput.command() = GrantSlackAccessCommand(code, state)
