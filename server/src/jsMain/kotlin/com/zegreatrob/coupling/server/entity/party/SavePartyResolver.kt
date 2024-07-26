package com.zegreatrob.coupling.server.entity.party

import com.zegreatrob.coupling.action.party.SavePartyCommand
import com.zegreatrob.coupling.action.party.perform
import com.zegreatrob.coupling.json.GqlSavePartyInput
import com.zegreatrob.coupling.json.toModel
import com.zegreatrob.coupling.server.entity.boost.requiredInput
import com.zegreatrob.coupling.server.graphql.DispatcherProviders.command
import com.zegreatrob.coupling.server.graphql.dispatch
import kotlinx.serialization.json.JsonNull

val savePartyResolver = dispatch(
    dispatcherFunc = command(),
    commandFunc = requiredInput { _: JsonNull, input: GqlSavePartyInput -> SavePartyCommand(input.toModel()) },
    fireFunc = ::perform,
    toSerializable = { true },
)
