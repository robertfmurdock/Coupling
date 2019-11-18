package com.zegreatrob.coupling.server.entity.user

import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.server.action.user.UserIsAuthorizedAction
import com.zegreatrob.coupling.server.action.user.UserIsAuthorizedActionDispatcher
import com.zegreatrob.coupling.server.entity.tribe.ScopeSyntax

interface UserIsAuthorizedActionDispatcherJs : UserIsAuthorizedActionDispatcher, ScopeSyntax {
    suspend fun performUserIsAuthorizedAction(tribeId: String) = UserIsAuthorizedAction(TribeId(tribeId))
        .perform()
}
