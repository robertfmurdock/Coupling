package com.zegreatrob.coupling.server.entity.slackaccess

import com.zegreatrob.coupling.action.GrantSlackAccessCommand
import com.zegreatrob.coupling.action.VoidResult
import com.zegreatrob.coupling.action.perform
import com.zegreatrob.coupling.json.GqlGrantSlackAccessInput
import com.zegreatrob.coupling.server.entity.boost.requiredInput
import com.zegreatrob.coupling.server.entity.toJson
import com.zegreatrob.coupling.server.external.graphql.Resolver
import com.zegreatrob.coupling.server.graphql.DispatcherProviders
import com.zegreatrob.coupling.server.graphql.dispatch
import kotlinx.serialization.json.JsonNull

val grantSlackAccessResolver: Resolver = dispatch(
    DispatcherProviders.command(),
    commandFunc = requiredInput { _: JsonNull, input: GqlGrantSlackAccessInput -> input.command() },
    fireFunc = ::perform,
    toSerializable = VoidResult::toJson,
)

private fun GqlGrantSlackAccessInput.command() = GrantSlackAccessCommand(code, state)
