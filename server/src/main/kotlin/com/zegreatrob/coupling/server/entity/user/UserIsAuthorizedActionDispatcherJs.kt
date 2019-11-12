package com.zegreatrob.coupling.server.entity.user

import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.server.action.user.UserIsAuthorizedAction
import com.zegreatrob.coupling.server.action.user.UserIsAuthorizedActionDispatcher
import com.zegreatrob.coupling.server.entity.tribe.ScopeSyntax
import kotlinx.coroutines.promise

interface UserIsAuthorizedActionDispatcherJs : UserIsAuthorizedActionDispatcher, ScopeSyntax {
    @JsName("performUserIsAuthorizedAction")
    fun performUserIsAuthorizedAction(tribeId: String) = scope.promise {
        UserIsAuthorizedAction(TribeId(tribeId))
            .perform()
    }
}
