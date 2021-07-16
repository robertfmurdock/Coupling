package com.zegreatrob.coupling.server.entity.player

import com.zegreatrob.coupling.json.SavePlayerInput
import com.zegreatrob.coupling.json.toModel
import com.zegreatrob.coupling.json.toSerializable
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.server.action.player.SavePlayerCommand
import com.zegreatrob.coupling.server.graphql.DispatcherProviders.tribeCommand
import com.zegreatrob.coupling.server.graphql.dispatch

val savePlayerResolver =
    dispatch(tribeCommand, { _, args: SavePlayerInput -> SavePlayerCommand(args.toModel()) }, Player::toSerializable)
