package com.zegreatrob.coupling.server.entity.player

import com.zegreatrob.coupling.json.toJsonArray
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.player.TribeIdPlayer
import com.zegreatrob.coupling.server.action.player.PlayersQuery
import com.zegreatrob.coupling.server.graphql.dispatchCommand
import com.zegreatrob.coupling.server.graphql.tribeCommandDispatcher

val playerListResolve = dispatchCommand(
    ::tribeCommandDispatcher,
    { PlayersQuery },
    List<Record<TribeIdPlayer>>::toJsonArray
)
