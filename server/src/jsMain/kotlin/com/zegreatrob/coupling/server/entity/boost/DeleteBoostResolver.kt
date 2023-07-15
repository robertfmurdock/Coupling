package com.zegreatrob.coupling.server.entity.boost

import com.zegreatrob.coupling.action.boost.DeleteBoostCommand
import com.zegreatrob.coupling.action.boost.perform
import com.zegreatrob.coupling.server.graphql.DispatcherProviders.prereleaseCommand
import com.zegreatrob.coupling.server.graphql.dispatchAction
import kotlinx.serialization.json.JsonNull

val deleteBoostResolver = dispatchAction(
    dispatcherFunc = prereleaseCommand(),
    commandFunc = { _: JsonNull, _: JsonNull -> DeleteBoostCommand() },
    fireFunc = ::perform,
    toSerializable = { true },
)
