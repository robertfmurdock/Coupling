package com.zegreatrob.coupling.server.entity.user

import com.zegreatrob.coupling.json.toJson
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.server.action.user.UserIsAuthorizedWithDataAction
import com.zegreatrob.coupling.server.action.user.UserIsAuthorizedWithDataActionDispatcher
import com.zegreatrob.coupling.server.entity.tribe.ScopeSyntax
import kotlinx.coroutines.promise
import kotlin.js.json

interface UserIsAuthorizedWithDataActionDispatcherJs : UserIsAuthorizedWithDataActionDispatcher, ScopeSyntax {
    @JsName("performUserIsAuthorizedWithDataAction")
    fun performUserIsAuthorizedWithDataAction(tribeId: String) = scope.promise {
        UserIsAuthorizedWithDataAction(TribeId(tribeId))
            .perform()
            ?.let { (tribe, players) ->
                json("tribe" to tribe.toJson(), "players" to players.map { it.toJson() }.toTypedArray())
            }
    }
}
