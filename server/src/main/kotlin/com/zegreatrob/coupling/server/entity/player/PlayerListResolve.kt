package com.zegreatrob.coupling.server.entity.player

import com.zegreatrob.coupling.json.toJsonArray
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.player.TribeIdPlayer
import com.zegreatrob.coupling.server.action.player.PlayersQuery
import com.zegreatrob.coupling.server.graphql.dispatchTribeCommand

val playerListResolve = dispatchTribeCommand(::query, { it.perform() }, List<Record<TribeIdPlayer>>::toJsonArray)

private fun query() = PlayersQuery