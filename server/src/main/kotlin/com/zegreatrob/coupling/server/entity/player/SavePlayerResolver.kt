package com.zegreatrob.coupling.server.entity.player

import com.zegreatrob.coupling.json.toJson
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.player.defaultPlayer
import com.zegreatrob.coupling.server.action.player.SavePlayerCommand
import com.zegreatrob.coupling.server.graphql.DispatcherProviders.tribeCommand
import com.zegreatrob.coupling.server.graphql.dispatch
import com.zegreatrob.minjson.at
import kotlin.js.Json

val savePlayerResolver = dispatch(
    tribeCommand,
    { _, args -> args.toPlayer().let(::SavePlayerCommand) },
    Player::toJson
)

private fun Json.toPlayer() = Player(
    id = at("/input/playerId") ?: "",
    badge = at("/input/badge") ?: defaultPlayer.badge,
    name = at("/input/name") ?: defaultPlayer.name,
    email = at("/input/email") ?: defaultPlayer.email,
    callSignAdjective = at("/input/callSignAdjective") ?: defaultPlayer.callSignAdjective,
    callSignNoun = at("/input/callSignNoun") ?: defaultPlayer.callSignNoun,
    imageURL = at("/input/imageURL")
)
