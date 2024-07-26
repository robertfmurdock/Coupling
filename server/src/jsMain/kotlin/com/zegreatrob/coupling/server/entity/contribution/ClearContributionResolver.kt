package com.zegreatrob.coupling.server.entity.contribution

import com.zegreatrob.coupling.action.party.ClearContributionsCommand
import com.zegreatrob.coupling.action.party.perform
import com.zegreatrob.coupling.json.GqlClearContributionsInput
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.server.entity.boost.requiredInput
import com.zegreatrob.coupling.server.graphql.DispatcherProviders
import com.zegreatrob.coupling.server.graphql.dispatch
import kotlinx.serialization.json.JsonNull

val clearContributionsResolver = dispatch(
    dispatcherFunc = DispatcherProviders.command(),
    commandFunc = requiredInput { _: JsonNull, args: GqlClearContributionsInput -> args.toCommand() },
    fireFunc = ::perform,
    toSerializable = { true },
)

private fun GqlClearContributionsInput.toCommand() = ClearContributionsCommand(
    partyId = PartyId(partyId),
)
