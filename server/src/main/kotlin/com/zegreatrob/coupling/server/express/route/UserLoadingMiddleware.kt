package com.zegreatrob.coupling.server.express.route

import com.benasher44.uuid.uuid4
import com.zegreatrob.coupling.action.valueOrNull
import com.zegreatrob.coupling.model.party.PartyId
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
            val userEmail = auth["https://zegreatrob.com/email"].asDynamic()
            if (userEmail == null) {
                val secretId = "${auth["https://zegreatrob.com/secret-id"]}"
                val partyId = "${auth["sub"]}"
                User(id = secretId, email = secretId, authorizedPartyIds = setOf(PartyId(partyId)))
            } else {
                UserDataService.authActionDispatcher("$userEmail", uuid4())
                    .invoke(FindOrCreateUserAction)
                    .valueOrNull()
            }
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
