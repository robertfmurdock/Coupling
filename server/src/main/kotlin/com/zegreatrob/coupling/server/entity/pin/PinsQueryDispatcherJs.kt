package com.zegreatrob.coupling.server.entity.pin

import com.zegreatrob.coupling.json.toJsonArray
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.server.EndpointHandlerSyntax
import com.zegreatrob.coupling.server.ResponseHelpers.sendQueryResults
import com.zegreatrob.coupling.server.action.pin.PinsQuery
import com.zegreatrob.coupling.server.action.pin.PinsQueryDispatcher
import com.zegreatrob.coupling.server.action.user.UserIsAuthorizedAction
import com.zegreatrob.coupling.server.action.user.UserIsAuthorizedActionDispatcher
import com.zegreatrob.coupling.server.entity.tribe.RequestTribeIdSyntax
import kotlinx.coroutines.promise

interface PinsQueryDispatcherJs : PinsQueryDispatcher, RequestTribeIdSyntax, EndpointHandlerSyntax,
    UserIsAuthorizedActionDispatcher {
    val performPinsQuery
        get() = endpointHandler(sendQueryResults("pin")) {
            PinsQuery
                .perform()
                ?.toJsonArray()
        }

    @JsName("performPinListQueryGQL")
    fun performPinListQueryGQL() = scope.promise {
        PinsQuery
            .perform()
            ?.toJsonArray()
    }

    private suspend fun userIsAuthorized(tribeId: TribeId) = UserIsAuthorizedAction(tribeId).perform()
}
