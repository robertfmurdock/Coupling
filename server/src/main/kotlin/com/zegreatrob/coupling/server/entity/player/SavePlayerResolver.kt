package com.zegreatrob.coupling.server.entity.player

import com.zegreatrob.coupling.json.stringValue
import com.zegreatrob.coupling.json.toIntFromStringOrInt
import com.zegreatrob.coupling.json.toJson
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.player.defaultPlayer
import com.zegreatrob.coupling.server.action.player.SavePlayerCommand
import com.zegreatrob.coupling.server.graphql.DispatcherProviders.tribeCommand
import com.zegreatrob.coupling.server.graphql.dispatch
import kotlin.js.Json

val savePlayerResolver = dispatch(
    tribeCommand,
    { _, args ->
        args.savePlayerInput()
            .toPlayer()
            .let(::SavePlayerCommand)
    },
    Player::toJson
)

private fun Json.savePlayerInput() = this["input"].unsafeCast<Json>()

private fun Json.toPlayer() = Player(
    id = stringValue("playerId") ?: "",
    badge = this["badge"]?.toIntFromStringOrInt() ?: defaultPlayer.badge,
    name = stringValue("name") ?: defaultPlayer.name,
    email = stringValue("email") ?: defaultPlayer.email,
    callSignAdjective = stringValue("callSignAdjective") ?: defaultPlayer.callSignAdjective,
    callSignNoun = stringValue("callSignNoun") ?: defaultPlayer.callSignNoun,
    imageURL = stringValue("imageURL")
)

