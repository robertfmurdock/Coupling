package com.zegreatrob.coupling.server.entity.player

import com.zegreatrob.coupling.json.SavePlayerInput
import com.zegreatrob.coupling.json.couplingJsonFormat
import com.zegreatrob.coupling.json.toModel
import com.zegreatrob.coupling.json.toSerializable
import com.zegreatrob.coupling.server.action.player.SavePlayerCommand
import com.zegreatrob.coupling.server.graphql.DispatcherProviders.tribeCommand
import com.zegreatrob.coupling.server.graphql.dispatch
import com.zegreatrob.minjson.at
import kotlinx.serialization.json.decodeFromDynamic
import kotlinx.serialization.json.encodeToDynamic
import kotlin.js.Json

val savePlayerResolver = dispatch(
    tribeCommand,
    { _, args -> args.at<Json>("/input").asInput().toModel().let(::SavePlayerCommand) }
) { couplingJsonFormat.encodeToDynamic(it.toSerializable()).unsafeCast<Json>() }

private fun Json?.asInput(): SavePlayerInput = couplingJsonFormat.decodeFromDynamic(this)
