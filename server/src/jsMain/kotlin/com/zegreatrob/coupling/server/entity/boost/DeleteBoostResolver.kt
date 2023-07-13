package com.zegreatrob.coupling.server.entity.boost

import com.zegreatrob.coupling.action.boost.DeleteBoostCommand
import com.zegreatrob.coupling.server.graphql.DispatcherProviders.prereleaseCommand
import com.zegreatrob.coupling.server.graphql.dispatch
import kotlinx.serialization.json.JsonNull

val deleteBoostResolver = dispatch(
    dispatcherFunc = prereleaseCommand(),
    queryFunc = { _: JsonNull, _: JsonNull -> DeleteBoostCommand() },
    toSerializable = { true },
)
