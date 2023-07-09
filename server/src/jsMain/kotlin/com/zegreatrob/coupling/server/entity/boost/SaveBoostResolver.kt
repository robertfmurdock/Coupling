package com.zegreatrob.coupling.server.entity.boost

import com.zegreatrob.coupling.action.boost.SaveBoostCommand
import com.zegreatrob.coupling.json.SaveBoostInput
import com.zegreatrob.coupling.server.graphql.DispatcherProviders.prereleaseCommand
import com.zegreatrob.coupling.server.graphql.dispatch
import kotlinx.serialization.json.JsonNull

val saveBoostResolver = dispatch(
    prereleaseCommand(),
    { _: JsonNull, args: SaveBoostInput -> SaveBoostCommand(args.partyIds) },
    { true },
)
