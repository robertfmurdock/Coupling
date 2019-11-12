package com.zegreatrob.coupling.server.entity.tribe

import com.zegreatrob.coupling.server.DeleteTribeCommand
import com.zegreatrob.coupling.server.DeleteTribeCommandDispatcher
import com.zegreatrob.coupling.server.external.express.Request
import com.zegreatrob.coupling.server.external.express.Response
import kotlinx.coroutines.promise
import kotlin.js.json

interface DeleteTribeCommandDispatcherJs : ScopeSyntax, DeleteTribeCommandDispatcher, RequestTribeIdSyntax,
    ResponseSendTribeNotFoundSyntax {

    @JsName("performDeleteTribeCommand")
    fun performDeleteTribeCommand(request: Request, response: Response) = scope.promise {
        DeleteTribeCommand(request.tribeId())
            .perform()
            .let { deleted -> sendResponse(deleted, response) }
    }

    private fun sendResponse(deleted: Boolean, response: Response) {
        if (deleted) {
            response.send(json())
        } else {
            response.sendTribeNotFound()
        }
    }

}
