package com.zegreatrob.coupling.server.entity.boost

import com.zegreatrob.coupling.action.SaveBoostCommand
import com.zegreatrob.coupling.action.perform
import com.zegreatrob.coupling.json.SaveBoostInput
import com.zegreatrob.coupling.server.graphql.DispatcherProviders.prereleaseCommand
import com.zegreatrob.coupling.server.graphql.dispatch
import kotlinx.serialization.json.JsonNull

val saveBoostResolver = dispatch(
    dispatcherFunc = prereleaseCommand(),
    commandFunc = { _: JsonNull, args: SaveBoostInput -> SaveBoostCommand(args.partyIds) },
    fireFunc = ::perform,
    toSerializable = { true },
)
