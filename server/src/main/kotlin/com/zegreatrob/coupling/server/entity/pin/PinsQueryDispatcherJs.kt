package com.zegreatrob.coupling.server.entity.pin

import com.zegreatrob.coupling.json.toJson
import com.zegreatrob.coupling.server.JsonSendToResponseSyntax
import com.zegreatrob.coupling.server.action.pin.PinsQuery
import com.zegreatrob.coupling.server.action.pin.PinsQueryDispatcher
import com.zegreatrob.coupling.server.entity.tribe.RequestTribeIdSyntax
import com.zegreatrob.coupling.server.entity.tribe.ScopeSyntax
import com.zegreatrob.coupling.server.external.express.Request
import com.zegreatrob.coupling.server.external.express.Response
import kotlinx.coroutines.promise

interface PinsQueryDispatcherJs : PinsQueryDispatcher, ScopeSyntax, RequestTribeIdSyntax, JsonSendToResponseSyntax {
    @JsName("performPinsQuery")
    fun performPinsQuery(request: Request, response: Response) = scope.promise {
        PinsQuery(request.tribeId())
            .perform()
            .map { it.toJson() }
            .toTypedArray()
            .sendTo(response)
    }
}
