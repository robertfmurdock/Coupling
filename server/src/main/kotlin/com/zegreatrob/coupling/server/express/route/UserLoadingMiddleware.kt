package com.zegreatrob.coupling.server.express.route

import com.benasher44.uuid.uuid4
import com.zegreatrob.coupling.action.valueOrNull
import com.zegreatrob.coupling.model.user.User
import com.zegreatrob.coupling.server.UserDataService
import com.zegreatrob.coupling.server.action.user.FindOrCreateUserAction
import com.zegreatrob.coupling.server.express.async
import com.zegreatrob.coupling.server.external.express.Handler
import com.zegreatrob.coupling.server.external.express.Request

fun userLoadingMiddleware(): Handler = { request, _, next ->
    val auth = request.auth
    if (auth == null) {
        request.setUser(null)
        next()
    } else {
        request.scope.async({ _, _ -> next() }) {
            UserDataService.authActionDispatcher("${auth["https://zegreatrob.com/email"]}", uuid4())
                .invoke(FindOrCreateUserAction)
                .valueOrNull()
                .let(request::setUser)
        }
    }
}

fun Request.setUser(user: User?) {
    with(asDynamic()) {
        this.user = user
        this.isAuthenticated = user != null
    }
}
