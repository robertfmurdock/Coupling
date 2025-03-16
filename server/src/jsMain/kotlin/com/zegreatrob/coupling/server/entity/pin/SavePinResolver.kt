package com.zegreatrob.coupling.server.entity.pin

import com.zegreatrob.coupling.action.pin.SavePinCommand
import com.zegreatrob.coupling.action.pin.perform
import com.zegreatrob.coupling.json.GqlSavePinInput
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.server.entity.boost.requiredInput
import com.zegreatrob.coupling.server.graphql.DispatcherProviders.authorizedPartyDispatcher
import com.zegreatrob.coupling.server.graphql.dispatch
import kotlinx.serialization.json.JsonNull

val savePinResolver = dispatch(
    dispatcherFunc = requiredInput { request, _: JsonNull, args: GqlSavePinInput ->
        authorizedPartyDispatcher(
            context = request,
            partyId = args.partyId,
        )
    },
    commandFunc = requiredInput { _, input -> input.toCommand() },
    fireFunc = ::perform,
    toSerializable = { true },
)

private fun GqlSavePinInput.toCommand() = SavePinCommand(partyId, toPin())

private fun GqlSavePinInput.toPin() = Pin(
    id = pinId,
    name = name,
    icon = icon,
)
