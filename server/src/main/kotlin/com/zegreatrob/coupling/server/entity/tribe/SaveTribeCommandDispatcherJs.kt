package com.zegreatrob.coupling.server.entity.tribe

import com.zegreatrob.coupling.json.toTribe
import com.zegreatrob.coupling.server.action.tribe.SaveTribeCommand
import com.zegreatrob.coupling.server.action.tribe.SaveTribeCommandDispatcher
import com.zegreatrob.coupling.server.external.express.Request
import kotlinx.coroutines.promise

interface SaveTribeCommandDispatcherJs : ScopeSyntax, SaveTribeCommandDispatcher {
    @JsName("performSaveTribeCommand")
    fun performSaveTribeCommand(request: Request) = scope.promise {
        request.saveTribeCommand()
            .perform()
    }

    private fun Request.saveTribeCommand() = SaveTribeCommand(body.toTribe())
}
