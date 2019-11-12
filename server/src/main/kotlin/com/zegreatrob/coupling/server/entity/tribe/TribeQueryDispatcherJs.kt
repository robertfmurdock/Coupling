package com.zegreatrob.coupling.server.entity.tribe

import com.zegreatrob.coupling.json.toJson
import com.zegreatrob.coupling.server.JsonSendAsResponseSyntax
import com.zegreatrob.coupling.server.action.tribe.TribeQuery
import com.zegreatrob.coupling.server.action.tribe.TribeQueryDispatcher
import com.zegreatrob.coupling.server.external.express.Request
import com.zegreatrob.coupling.server.external.express.Response
import kotlinx.coroutines.promise

interface TribeQueryDispatcherJs : TribeQueryDispatcher, ScopeSyntax, RequestTribeIdSyntax, JsonSendAsResponseSyntax,
    ResponseSendTribeNotFoundSyntax {

    @JsName("performTribeQuery")
    fun performTribeQuery(request: Request, response: Response) = scope.promise {
        TribeQuery(request.tribeId())
            .perform()
            ?.toJson()
            ?.sendTo(response)
            ?: response.sendTribeNotFound()
    }
}
