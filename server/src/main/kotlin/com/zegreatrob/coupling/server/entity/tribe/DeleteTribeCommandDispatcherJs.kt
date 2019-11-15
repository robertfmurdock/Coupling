package com.zegreatrob.coupling.server.entity.tribe

import com.zegreatrob.coupling.server.DeleteTribeCommand
import com.zegreatrob.coupling.server.DeleteTribeCommandDispatcher
import com.zegreatrob.coupling.server.EndpointHandlerSyntax
import com.zegreatrob.coupling.server.ResponseHelpers.sendDeleteResults

interface DeleteTribeCommandDispatcherJs : DeleteTribeCommandDispatcher, RequestTribeIdSyntax, EndpointHandlerSyntax {
    val performDeleteTribeCommand
        get() = endpointHandler(sendDeleteResults("Tribe")) {
            DeleteTribeCommand(tribeId())
                .perform()
        }

}
