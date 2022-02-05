package com.zegreatrob.coupling.server.entity.pin

import com.zegreatrob.coupling.json.DeletePinInput
import com.zegreatrob.coupling.server.action.pin.DeletePinCommand
import com.zegreatrob.coupling.server.graphql.DispatcherProviders.tribeCommand
import com.zegreatrob.coupling.server.graphql.dispatch

val deletePinResolver = dispatch(tribeCommand, { _, args: DeletePinInput -> DeletePinCommand(args.pinId) }, { true })
