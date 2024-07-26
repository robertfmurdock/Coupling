package com.zegreatrob.coupling.server.entity.boost

import com.zegreatrob.coupling.action.ApplyBoostCommand
import com.zegreatrob.coupling.action.perform
import com.zegreatrob.coupling.json.GqlSaveBoostInput
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.server.graphql.DispatcherProviders.prereleaseCommand
import com.zegreatrob.coupling.server.graphql.dispatch
import kotlinx.serialization.json.JsonNull

val saveBoostResolver = dispatch(
    dispatcherFunc = prereleaseCommand(),
    commandFunc = requiredInput { _: JsonNull, args: GqlSaveBoostInput -> ApplyBoostCommand(PartyId(args.partyId)) },
    fireFunc = ::perform,
    toSerializable = { true },
)
