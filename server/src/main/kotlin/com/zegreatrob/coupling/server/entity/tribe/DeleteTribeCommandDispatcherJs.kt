package com.zegreatrob.coupling.server.entity.tribe

import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.server.DeleteTribeCommand
import com.zegreatrob.coupling.server.DeleteTribeCommandDispatcher
import kotlinx.coroutines.promise

interface DeleteTribeCommandDispatcherJs: ScopeSyntax, DeleteTribeCommandDispatcher {

    @JsName("performDeleteTribeCommand")
    fun performDeleteTribeCommand(tribeId: String) = scope.promise {
        DeleteTribeCommand(TribeId(tribeId))
            .perform()
    }

}
