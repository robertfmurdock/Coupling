package com.zegreatrob.coupling.server.express.route

import com.benasher44.uuid.Uuid
import com.zegreatrob.coupling.action.LoggingActionPipe
import com.zegreatrob.coupling.action.valueOrNull
import com.zegreatrob.coupling.model.elements
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.party.Secret
import com.zegreatrob.coupling.model.user.User
import com.zegreatrob.coupling.server.UserDataService
import com.zegreatrob.coupling.server.action.user.FindOrCreateUserAction
import com.zegreatrob.coupling.server.express.async
import com.zegreatrob.coupling.server.external.express.Handler
import com.zegreatrob.coupling.server.external.express.Request
import com.zegreatrob.coupling.server.secretRepository
import com.zegreatrob.testmints.action.ActionCannon

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
                val partyId = PartyId("${auth["sub"]}")
                if (secretIsNotDeleted(secretId, partyId)) {
                    User(id = secretId, email = secretId, authorizedPartyIds = setOf(partyId))
                } else {
                    null
                }
            } else {
                authCannon(userEmail, request.traceId)
                    .fire(FindOrCreateUserAction)
                    .valueOrNull()
            }
                .let(request::setUser)
        }
    }
}

private suspend fun authCannon(userEmail: Any?, traceId: Uuid) = ActionCannon(
    UserDataService.authActionDispatcher("$userEmail", traceId),
    LoggingActionPipe(traceId),
)

private suspend fun secretIsNotDeleted(secretId: String, partyId: PartyId): Boolean = secretRepository(secretId)
    .getSecrets(partyId)
    .elements
    .contains(Secret(secretId))

fun Request.setUser(user: User?) {
    with(asDynamic()) {
        this.user = user
        this.isAuthenticated = user != null
    }
}
