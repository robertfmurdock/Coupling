package com.zegreatrob.coupling.server.entity.tribe

import com.zegreatrob.coupling.server.DeleteTribeCommand
import com.zegreatrob.coupling.server.DeleteTribeCommandDispatcher
import com.zegreatrob.coupling.server.EndpointHandlerSyntax
import com.zegreatrob.coupling.server.ResponseHelpers.sendDeleteResults
import com.zegreatrob.coupling.server.external.express.tribeId

interface DeleteTribeCommandDispatcherJs : DeleteTribeCommandDispatcher, EndpointHandlerSyntax {
    val performDeleteTribeCommand
        get() = endpointHandler(sendDeleteResults("Tribe")) {
            DeleteTribeCommand(tribeId())
                .perform()
        }

}
