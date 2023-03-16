package com.zegreatrob.coupling.server.entity.boost

import com.zegreatrob.coupling.json.SaveBoostInput
import com.zegreatrob.coupling.server.action.boost.SaveBoostCommand
import com.zegreatrob.coupling.server.graphql.DispatcherProviders.prereleaseCommand
import com.zegreatrob.coupling.server.graphql.dispatch

val saveBoostResolver = dispatch(
    prereleaseCommand,
    { _, args: SaveBoostInput -> SaveBoostCommand(args.partyIds) },
    { true },
)
