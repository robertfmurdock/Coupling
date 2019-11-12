package com.zegreatrob.coupling.server.entity.tribe

import com.zegreatrob.coupling.json.toTribe
import com.zegreatrob.coupling.server.JsonSendAsResponseSyntax
import com.zegreatrob.coupling.server.action.tribe.SaveTribeCommand
import com.zegreatrob.coupling.server.action.tribe.SaveTribeCommandDispatcher
import com.zegreatrob.coupling.server.external.express.Request
import com.zegreatrob.coupling.server.external.express.Response
import kotlinx.coroutines.promise

interface SaveTribeCommandDispatcherJs : ScopeSyntax, SaveTribeCommandDispatcher, JsonSendAsResponseSyntax {
    @JsName("performSaveTribeCommand")
    fun performSaveTribeCommand(request: Request, response: Response) = scope.promise {
        SaveTribeCommand(request.body.toTribe())
            .perform()
        request.body.sendTo(response)
    }

}
