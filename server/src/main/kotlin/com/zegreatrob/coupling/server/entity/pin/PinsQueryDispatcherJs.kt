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
            PinsQuery(tribeId())
                .perform()
                .toJsonArray()
        }

    @JsName("performPinListQueryGQL")
    fun performPinListQueryGQL(id: String) = scope.promise {
        val tribeId = TribeId(id)
        if (userIsAuthorized(tribeId)) {
            PinsQuery(tribeId)
                .perform()
                .toJsonArray()
        } else {
            null
        }
    }

    private suspend fun userIsAuthorized(tribeId: TribeId) = UserIsAuthorizedAction(tribeId).perform()
}
