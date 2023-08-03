package com.zegreatrob.coupling.server.entity.boost

import com.zegreatrob.coupling.action.boost.DeleteBoostCommand
import com.zegreatrob.coupling.action.boost.perform
import com.zegreatrob.coupling.server.express.route.CouplingContext
import com.zegreatrob.coupling.server.graphql.DispatcherProviders.prereleaseCommand
import com.zegreatrob.coupling.server.graphql.dispatch
import kotlinx.serialization.json.JsonNull
import kotlin.js.Json
import kotlin.js.Promise

val deleteBoostResolver: (Json, Json, CouplingContext, Json) -> Promise<dynamic> = dispatch(
    dispatcherFunc = prereleaseCommand(),
    commandFunc = { _: JsonNull, _: JsonNull -> DeleteBoostCommand() },
    fireFunc = ::perform,
    toSerializable = { true },
)
