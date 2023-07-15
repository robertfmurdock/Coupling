package com.zegreatrob.coupling.server.entity.party

import com.zegreatrob.coupling.action.party.SavePartyCommand
import com.zegreatrob.coupling.action.party.perform
import com.zegreatrob.coupling.json.SavePartyInput
import com.zegreatrob.coupling.json.toModel
import com.zegreatrob.coupling.server.graphql.DispatcherProviders.command
import com.zegreatrob.coupling.server.graphql.dispatchAction
import kotlinx.serialization.json.JsonNull

val savePartyResolver = dispatchAction(
    dispatcherFunc = command(),
    commandFunc = { _: JsonNull, input: SavePartyInput -> SavePartyCommand(input.toModel()) },
    fireFunc = ::perform,
    toSerializable = { true },
)
