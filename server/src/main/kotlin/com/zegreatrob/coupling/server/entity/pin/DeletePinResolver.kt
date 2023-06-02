package com.zegreatrob.coupling.server.entity.pin

import com.zegreatrob.coupling.json.DeletePinInput
import com.zegreatrob.coupling.server.action.pin.DeletePinCommand
import com.zegreatrob.coupling.server.graphql.DispatcherProviders.authorizedDispatcher
import com.zegreatrob.coupling.server.graphql.dispatch
import kotlinx.serialization.json.JsonNull

val deletePinResolver = dispatch(
    dispatcherFunc = { request, _: JsonNull, args -> authorizedDispatcher(request = request, partyId = args.partyId.value) },
    queryFunc = { _, args: DeletePinInput -> DeletePinCommand(args.pinId) },
    toSerializable = { true },
)
