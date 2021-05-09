package com.zegreatrob.coupling.server.entity.tribe

import com.zegreatrob.coupling.action.valueOrNull
import com.zegreatrob.coupling.server.action.user.UserIsAuthorizedAction
import com.zegreatrob.coupling.server.express.route.routerParams
import com.zegreatrob.coupling.server.external.express.*
import kotlinx.coroutines.launch


val tribeRouter by lazy {
    Router(routerParams(mergeParams = true)).apply {
        route("/*").all(::authCheck)
    }
}

private fun authCheck(request: Request, response: Response, next: Next): Unit = with(request) {
    scope.launch {
        if (execute(userIsAuthorizedAction()).valueOrNull() == true) {
            next()
        } else {
            response.sendStatus(404)
        }
    }
}

private val Request.execute get() = commandDispatcher.execute

private fun Request.userIsAuthorizedAction() = UserIsAuthorizedAction(tribeId())

val tribeListRouter by lazy {
    Router(routerParams()).apply {
        use("/:tribeId", tribeRouter)
    }
}