package com.zegreatrob.coupling.server.entity.discord

import com.zegreatrob.coupling.action.GrantDiscordAccessCommand
import com.zegreatrob.coupling.action.VoidResult
import com.zegreatrob.coupling.action.perform
import com.zegreatrob.coupling.json.GrantDiscordAccessInput
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.server.entity.boost.requiredInput
import com.zegreatrob.coupling.server.entity.toJson
import com.zegreatrob.coupling.server.external.graphql.Resolver
import com.zegreatrob.coupling.server.graphql.DispatcherProviders
import com.zegreatrob.coupling.server.graphql.dispatch
import kotlinx.serialization.json.JsonNull

val grantDiscordAccessResolver: Resolver = dispatch(
    DispatcherProviders.command(),
    commandFunc = requiredInput { _: JsonNull, input: GrantDiscordAccessInput -> input.command() },
    fireFunc = ::perform,
    toSerializable = VoidResult::toJson,
)

private fun GrantDiscordAccessInput.command() = GrantDiscordAccessCommand(code, guildId, PartyId(partyId))
