package com.zegreatrob.coupling.server.entity.pin

import com.zegreatrob.coupling.json.DeletePinInput
import com.zegreatrob.coupling.server.action.pin.DeletePinCommand
import com.zegreatrob.coupling.server.graphql.DispatcherProviders.partyCommand
import com.zegreatrob.coupling.server.graphql.dispatch

val deletePinResolver = dispatch(partyCommand, { _, args: DeletePinInput -> DeletePinCommand(args.pinId) }, { true })
