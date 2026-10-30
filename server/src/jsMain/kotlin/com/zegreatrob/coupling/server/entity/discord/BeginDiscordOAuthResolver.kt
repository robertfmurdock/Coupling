package com.zegreatrob.coupling.server.entity.discord

import com.zegreatrob.coupling.action.BeginDiscordOAuthCommand
import com.zegreatrob.coupling.action.perform
import com.zegreatrob.coupling.json.GqlBeginDiscordOAuthInput
import com.zegreatrob.coupling.json.GqlDiscordOAuthState
import com.zegreatrob.coupling.server.entity.boost.requiredInput
import com.zegreatrob.coupling.server.graphql.DispatcherProviders.authorizedPartyDispatcher
import com.zegreatrob.coupling.server.graphql.dispatch
import kotlinx.serialization.json.JsonNull

val beginDiscordOAuthResolver = dispatch(
    dispatcherFunc = requiredInput { request, _, args -> authorizedPartyDispatcher(request, args.partyId) },
    commandFunc = requiredInput { _: JsonNull, input: GqlBeginDiscordOAuthInput -> BeginDiscordOAuthCommand(input.partyId, input.challenge) },
    fireFunc = ::perform,
    toSerializable = { it?.let { state -> GqlDiscordOAuthState(state = state.state, expiresAt = state.expiresAt) } },
)
