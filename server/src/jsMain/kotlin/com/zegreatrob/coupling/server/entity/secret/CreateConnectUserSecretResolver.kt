package com.zegreatrob.coupling.server.entity.secret

import com.zegreatrob.coupling.action.user.CreateConnectUserSecretCommand
import com.zegreatrob.coupling.action.user.perform
import com.zegreatrob.coupling.json.toModel
import com.zegreatrob.coupling.server.graphql.DispatcherProviders
import com.zegreatrob.coupling.server.graphql.dispatch
import kotlinx.serialization.json.JsonNull

val createConnectUserSecretResolver = dispatch(
    dispatcherFunc = DispatcherProviders.command(),
    commandFunc = { _: JsonNull, _: JsonNull? -> CreateConnectUserSecretCommand },
    fireFunc = ::perform,
    toSerializable = { it?.toModel() },
)
