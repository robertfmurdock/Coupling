package com.zegreatrob.coupling.server.entity.tribe

import com.zegreatrob.coupling.server.DeleteTribeCommand
import com.zegreatrob.coupling.server.DeleteTribeCommandDispatcher
import com.zegreatrob.coupling.server.external.express.Request
import kotlinx.coroutines.promise

interface DeleteTribeCommandDispatcherJs: ScopeSyntax, DeleteTribeCommandDispatcher, RequestTribeIdSyntax {

    @JsName("performDeleteTribeCommand")
    fun performDeleteTribeCommand(request: Request) = scope.promise {
        DeleteTribeCommand(request.tribeId())
            .perform()
    }

}
