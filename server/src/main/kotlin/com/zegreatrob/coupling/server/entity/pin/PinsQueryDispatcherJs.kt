package com.zegreatrob.coupling.server.entity.pin

import com.zegreatrob.coupling.json.toJsonArray
import com.zegreatrob.coupling.server.EndpointHandlerSyntax
import com.zegreatrob.coupling.server.ResponseHelpers.sendQueryResults
import com.zegreatrob.coupling.server.action.pin.PinsQuery
import com.zegreatrob.coupling.server.action.pin.PinsQueryDispatcher
import com.zegreatrob.coupling.server.entity.tribe.RequestTribeIdSyntax

interface PinsQueryDispatcherJs : PinsQueryDispatcher, RequestTribeIdSyntax, EndpointHandlerSyntax {
    val performPinsQuery
        get() = endpointHandler(sendQueryResults("pin")) {
            PinsQuery(tribeId())
                .perform()
                .toJsonArray()
        }
}
