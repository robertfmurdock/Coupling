package com.zegreatrob.coupling.server.entity.tribe

import com.zegreatrob.coupling.json.toJson
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.server.EndpointHandlerSyntax
import com.zegreatrob.coupling.server.ResponseHelpers.sendQueryResults
import com.zegreatrob.coupling.server.action.tribe.TribeQuery
import com.zegreatrob.coupling.server.action.tribe.TribeQueryDispatcher
import kotlinx.coroutines.promise

interface TribeQueryDispatcherJs : TribeQueryDispatcher, RequestTribeIdSyntax, EndpointHandlerSyntax {
    val performTribeQuery
        get() = endpointHandler(sendQueryResults("tribe")) {
            TribeQuery(tribeId())
                .perform()
                ?.toJson()
        }

    @JsName("performTribeQueryGQL")
    fun performTribeQueryGQL(id: String) = scope.promise {
        TribeQuery(TribeId(id))
            .perform()
            ?.toJson()
    }
}
