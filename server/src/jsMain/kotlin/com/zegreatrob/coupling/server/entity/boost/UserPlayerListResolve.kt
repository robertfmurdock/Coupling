package com.zegreatrob.coupling.server.entity.boost

import com.zegreatrob.coupling.json.toSerializable
import com.zegreatrob.coupling.model.PartyRecord
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.server.action.player.UserPlayersQuery
import com.zegreatrob.coupling.server.action.player.perform
import com.zegreatrob.coupling.server.graphql.DispatcherProviders
import com.zegreatrob.coupling.server.graphql.dispatch
import kotlinx.serialization.json.JsonNull

val userPlayerListResolve = dispatch(
    dispatcherFunc = DispatcherProviders.command(),
    commandFunc = { _: JsonNull, _: JsonNull? -> UserPlayersQuery },
    fireFunc = ::perform,
    toSerializable = { it.map(PartyRecord<Player>::toSerializable) },
)
