package com.zegreatrob.coupling.server.entity.boost

import com.zegreatrob.coupling.server.action.boost.DeleteBoostCommand
import com.zegreatrob.coupling.server.graphql.DispatcherProviders.prereleaseCommand
import com.zegreatrob.coupling.server.graphql.dispatch
import kotlinx.serialization.json.JsonElement

val deleteBoostResolver = dispatch(
    prereleaseCommand,
    { _, _: JsonElement -> DeleteBoostCommand() },
    { true },
)
