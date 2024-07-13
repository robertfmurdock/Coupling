package com.zegreatrob.coupling.server.entity.pin

import com.zegreatrob.coupling.action.pin.DeletePinCommand
import com.zegreatrob.coupling.action.pin.perform
import com.zegreatrob.coupling.json.DeletePinInput
import com.zegreatrob.coupling.server.entity.boost.requiredInput
import com.zegreatrob.coupling.server.graphql.DispatcherProviders.authorizedPartyDispatcher
import com.zegreatrob.coupling.server.graphql.dispatch
import kotlinx.serialization.json.JsonNull

val deletePinResolver = dispatch(
    dispatcherFunc = requiredInput { request, _: JsonNull, args: DeletePinInput ->
        authorizedPartyDispatcher(
            context = request,
            partyId = args.partyId.value,
        )
    },
    commandFunc = requiredInput { _, input -> input.toCommand() },
    fireFunc = ::perform,
    toSerializable = { true },
)

private fun DeletePinInput.toCommand() = DeletePinCommand(partyId, pinId)
