package com.zegreatrob.coupling.server.entity.tribe

import com.zegreatrob.coupling.json.toJson
import com.zegreatrob.coupling.server.JsonSendAsResponseSyntax
import com.zegreatrob.coupling.server.action.tribe.TribeQuery
import com.zegreatrob.coupling.server.action.tribe.TribeQueryDispatcher
import com.zegreatrob.coupling.server.external.express.Request
import com.zegreatrob.coupling.server.external.express.Response
import kotlinx.coroutines.promise
import kotlin.js.json

interface TribeQueryDispatcherJs : TribeQueryDispatcher, ScopeSyntax, RequestTribeIdSyntax, JsonSendAsResponseSyntax {

    @JsName("performTribeQuery")
    fun performTribeQuery(request: Request, response: Response) = scope.promise {
        TribeQuery(request.tribeId())
            .perform()
            ?.toJson()
            ?.sendAs(response)
            ?: sendTribeNotFound(response)
    }

    private fun sendTribeNotFound(response: Response) {
        json("message" to "tribe not found")
            .sendAs(response, 404)
    }

}
