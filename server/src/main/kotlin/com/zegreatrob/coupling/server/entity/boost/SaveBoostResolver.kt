package com.zegreatrob.coupling.server.entity.boost

import com.zegreatrob.coupling.json.SaveBoostInput
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.server.action.boost.SaveBoostCommand
import com.zegreatrob.coupling.server.graphql.DispatcherProviders.prereleaseTribeCommand
import com.zegreatrob.coupling.server.graphql.dispatch

val saveBoostResolver = dispatch(
    prereleaseTribeCommand,
    { _, args: SaveBoostInput -> SaveBoostCommand(args.id, args.tribeIds.map(::TribeId).toSet()) },
    { true }
)
