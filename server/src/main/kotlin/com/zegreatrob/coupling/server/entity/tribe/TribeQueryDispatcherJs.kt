package com.zegreatrob.coupling.server.entity.tribe

import com.zegreatrob.coupling.json.toJson
import com.zegreatrob.coupling.server.EndpointHandlerSyntax
import com.zegreatrob.coupling.server.ResponseHelpers.sendQueryResults
import com.zegreatrob.coupling.server.action.tribe.TribeQuery
import com.zegreatrob.coupling.server.action.tribe.TribeQueryDispatcher

interface TribeQueryDispatcherJs : TribeQueryDispatcher, RequestTribeIdSyntax, EndpointHandlerSyntax {
    val performTribeQuery
        get() = endpointHandler(sendQueryResults("tribe")) {
            TribeQuery(tribeId())
                .perform()
                ?.toJson()
        }

}
