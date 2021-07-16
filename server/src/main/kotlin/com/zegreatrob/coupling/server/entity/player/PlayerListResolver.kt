package com.zegreatrob.coupling.server.entity.player

import com.zegreatrob.coupling.json.toSerializable
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.player.TribeIdPlayer
import com.zegreatrob.coupling.server.action.player.PlayersQuery
import com.zegreatrob.coupling.server.graphql.DispatcherProviders.tribeCommand
import com.zegreatrob.coupling.server.graphql.dispatch
import kotlinx.serialization.json.JsonElement

val playerListResolve = dispatch(tribeCommand, { _, _: JsonElement -> PlayersQuery }) {
    it.map(Record<TribeIdPlayer>::toSerializable)
}
