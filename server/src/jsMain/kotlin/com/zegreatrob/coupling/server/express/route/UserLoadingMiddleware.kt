package com.zegreatrob.coupling.server.express.route

import com.benasher44.uuid.Uuid
import com.zegreatrob.coupling.action.LoggingActionPipe
import com.zegreatrob.coupling.action.valueOrNull
import com.zegreatrob.coupling.model.elements
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.party.Secret
import com.zegreatrob.coupling.model.party.SecretUsed
import com.zegreatrob.coupling.model.user.UserDetails
import com.zegreatrob.coupling.repository.secret.SecretRepository
import com.zegreatrob.coupling.server.UserDataService
import com.zegreatrob.coupling.server.action.user.FindOrCreateUserAction
import com.zegreatrob.coupling.server.express.async
import com.zegreatrob.coupling.server.external.express.Handler
import com.zegreatrob.coupling.server.external.express.Request
import com.zegreatrob.coupling.server.secretRepository
import com.zegreatrob.testmints.action.ActionCannon
import kotlinx.datetime.Clock

fun userLoadingMiddleware(): Handler = { request, _, next ->
    val auth = request.auth
    if (auth == null) {
        request.setUser(null)
        next()
    } else {
        request.scope.async({ _, _ -> next() }) {
            val userEmail = auth["https://zegreatrob.com/email"].asDynamic()
            if (userEmail == null) {
                userFromSecret(
                    partyId = PartyId("${auth["sub"]}"),
                    secretId = "${auth["https://zegreatrob.com/secret-id"]}",
                )
            } else {
                authCannon(userEmail, request.traceId)
                    .fire(FindOrCreateUserAction)
                    .valueOrNull()
            }
                .let(request::setUser)
        }
    }
}

private suspend fun userFromSecret(partyId: PartyId, secretId: String): UserDetails? {
    val secretRepository = secretRepository(secretId)
    return if (!secretRepository.secretIsNotDeleted(partyId, secretId)) {
        null
    } else {
        secretRepository.save(SecretUsed(partyId, secretId, Clock.System.now()))

        UserDetails(
            id = secretId,
            email = secretId,
            authorizedPartyIds = setOf(partyId),
            stripeCustomerId = null,
        )
    }
}

private suspend fun authCannon(userEmail: Any?, traceId: Uuid) = ActionCannon(
    UserDataService.authActionDispatcher("$userEmail", traceId),
    LoggingActionPipe(traceId),
)

private suspend fun SecretRepository.secretIsNotDeleted(partyId: PartyId, secretId: String): Boolean = getSecrets(partyId)
    .elements
    .map(Secret::id)
    .contains(secretId)

fun Request.setUser(user: UserDetails?) {
    with(asDynamic()) {
        this.user = user
        this.isAuthenticated = user != null
    }
}
