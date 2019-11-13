package com.zegreatrob.coupling.server.entity.pin

import com.zegreatrob.coupling.server.EndpointHandlerSyntax
import com.zegreatrob.coupling.server.ResponseHelpers.sendDeleteResults
import com.zegreatrob.coupling.server.action.pin.DeletePinCommand
import com.zegreatrob.coupling.server.action.pin.DeletePinCommandDispatcher
import com.zegreatrob.coupling.server.entity.tribe.RequestTribeIdSyntax

interface DeletePinCommandDispatcherJs : DeletePinCommandDispatcher, RequestTribeIdSyntax, RequestPinIdSyntax,
    EndpointHandlerSyntax {

    @JsName("performDeletePinCommand")
    val performDeletePinCommand
        get() = endpointHandler(sendDeleteResults("Pin")) {
            DeletePinCommand(tribeId(), pinId())
                .perform()
        }
}
