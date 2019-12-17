package com.zegreatrob.coupling.server.entity.tribe

import com.zegreatrob.coupling.json.toJson
import com.zegreatrob.coupling.server.EndpointHandlerSyntax
import com.zegreatrob.coupling.server.action.tribe.TribeListQuery
import com.zegreatrob.coupling.server.action.tribe.TribeListQueryDispatcher
import com.zegreatrob.coupling.server.external.express.Response
import com.zegreatrob.coupling.server.external.express.sendSuccessful
import kotlinx.coroutines.promise

interface TribeListQueryDispatcherJs : TribeListQueryDispatcher, EndpointHandlerSyntax {

    val performTribeListQuery
        get() = endpointHandler(Response::sendSuccessful) {
            TribeListQuery
                .perform()
                .map { it.toJson() }
                .toTypedArray()
        }

    @JsName("performTribeListQueryGQL")
    fun performTribeListQueryGQL() = scope.promise {
        TribeListQuery
            .perform()
            .map { it.toJson() }
            .toTypedArray()
    }
}
