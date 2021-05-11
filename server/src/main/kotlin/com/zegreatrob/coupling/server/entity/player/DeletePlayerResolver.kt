package com.zegreatrob.coupling.server.entity.player

import com.zegreatrob.coupling.server.action.player.DeletePlayerCommand
import com.zegreatrob.coupling.server.graphql.DispatcherProviders.tribeCommand
import com.zegreatrob.coupling.server.graphql.dispatch
import com.zegreatrob.minjson.at

val deletePlayerResolver = dispatch(
    tribeCommand,
    { _, args -> DeletePlayerCommand(args.at<String>("/input/playerId")!!) },
    { true }
)
