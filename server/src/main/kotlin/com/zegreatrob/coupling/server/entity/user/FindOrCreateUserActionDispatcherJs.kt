package com.zegreatrob.coupling.server.entity.user

import com.zegreatrob.coupling.json.toJson
import com.zegreatrob.coupling.server.action.user.FindOrCreateUserAction
import com.zegreatrob.coupling.server.action.user.FindOrCreateUserActionDispatcher
import com.zegreatrob.coupling.server.entity.tribe.ScopeSyntax
import kotlinx.coroutines.promise

interface FindOrCreateUserActionDispatcherJs : FindOrCreateUserActionDispatcher, ScopeSyntax {

    fun performFindOrCreateUserAction() = scope.promise {
        FindOrCreateUserAction.perform()
            .toJson()
    }
}
