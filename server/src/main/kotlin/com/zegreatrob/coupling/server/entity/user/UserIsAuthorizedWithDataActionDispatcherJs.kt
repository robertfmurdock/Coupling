package com.zegreatrob.coupling.server.entity.user

import com.zegreatrob.coupling.json.toJson
import com.zegreatrob.coupling.server.action.user.UserIsAuthorizedWithDataAction
import com.zegreatrob.coupling.server.action.user.UserIsAuthorizedWithDataActionDispatcher
import com.zegreatrob.coupling.server.entity.tribe.RequestTribeIdSyntax
import com.zegreatrob.coupling.server.entity.tribe.ScopeSyntax
import com.zegreatrob.coupling.server.external.express.Request
import kotlinx.coroutines.promise
import kotlin.js.json

interface UserIsAuthorizedWithDataActionDispatcherJs : UserIsAuthorizedWithDataActionDispatcher, RequestTribeIdSyntax,
    ScopeSyntax {
    @JsName("performUserIsAuthorizedWithDataAction")
    fun performUserIsAuthorizedWithDataAction(request: Request) = scope.promise {
        UserIsAuthorizedWithDataAction(request.tribeId())
            .perform()
            ?.let { (tribe, players) ->
                json("tribe" to tribe.toJson(), "players" to players.map { it.toJson() }.toTypedArray())
            }
    }
}
