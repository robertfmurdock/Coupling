package com.zegreatrob.coupling.server.entity.secret

import com.zegreatrob.coupling.action.user.ConnectUserCommand
import com.zegreatrob.coupling.action.user.perform
import com.zegreatrob.coupling.json.GqlConnectUserInput
import com.zegreatrob.coupling.server.entity.boost.requiredInput
import com.zegreatrob.coupling.server.graphql.DispatcherProviders
import com.zegreatrob.coupling.server.graphql.dispatch
import kotlinx.serialization.json.JsonNull

val connectUserResolver = dispatch(
    dispatcherFunc = DispatcherProviders.command(),
    commandFunc = requiredInput { _: JsonNull, input: GqlConnectUserInput -> ConnectUserCommand(input.secretToken) },
    fireFunc = ::perform,
    toSerializable = { it },
)
