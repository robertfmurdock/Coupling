package com.zegreatrob.coupling.server.entity.pin

import com.zegreatrob.coupling.server.action.pin.DeletePinCommand
import com.zegreatrob.coupling.server.graphql.DispatcherProviders.tribeCommand
import com.zegreatrob.coupling.server.graphql.dispatch
import com.zegreatrob.minjson.at

val deletePinResolver = dispatch(
    tribeCommand,
    { _, args -> args.at<String>("/input/pinId")!!.let(::DeletePinCommand) },
    { true }
)
