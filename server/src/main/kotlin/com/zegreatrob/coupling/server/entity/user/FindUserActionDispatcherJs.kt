package com.zegreatrob.coupling.server.entity.user

import com.zegreatrob.coupling.server.action.user.FindUserAction
import com.zegreatrob.coupling.server.action.user.FindUserActionDispatcher
import com.zegreatrob.coupling.server.entity.tribe.ScopeSyntax
import kotlinx.coroutines.promise

interface FindUserActionDispatcherJs : FindUserActionDispatcher, ScopeSyntax, UserJsonSyntax {

    @JsName("performFindUserAction")
    fun performFindUserAction() = scope.promise {
        FindUserAction.perform()
            ?.toJson()
    }

}
