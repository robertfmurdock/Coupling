package com.zegreatrob.coupling.server.express.route

import com.zegreatrob.coupling.action.LoggingActionPipe
import com.zegreatrob.coupling.action.valueOrNull
import com.zegreatrob.coupling.model.elements
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.party.Secret
import com.zegreatrob.coupling.model.party.SecretId
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
import kotlin.uuid.Uuid

fun userLoadingMiddleware(): Handler = { request, _, next ->
    val auth = request.auth
    if (auth == null) {
        request.setUser(null)
        next()
    } else {
        request.scope.async({ _, _ -> next() }) {
            val userEmail = auth["https://zegreatrob.com/email"].asDynamic()
            val secretId = SecretId("${auth["https://zegreatrob.com/secret-id"]}")
            if (userEmail == null && secretId != null) {
                userFromSecret(
                    partyId = PartyId("${auth["sub"]}"),
                    secretId = secretId,
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

private suspend fun userFromSecret(partyId: PartyId, secretId: SecretId): UserDetails? {
    val secretUserId = secretId.value.toString()
    val secretRepository = secretRepository(secretUserId)
    return if (!secretRepository.secretIsNotDeleted(partyId, secretId)) {
        null
    } else {
        secretRepository.save(SecretUsed(partyId, secretId, Clock.System.now()))

        UserDetails(
            id = secretUserId,
            email = secretUserId,
            authorizedPartyIds = setOf(partyId),
            stripeCustomerId = null,
        )
    }
}

private suspend fun authCannon(userEmail: Any?, traceId: Uuid) = ActionCannon(
    UserDataService.authActionDispatcher("$userEmail", traceId),
    LoggingActionPipe(traceId),
)

private suspend fun SecretRepository.secretIsNotDeleted(partyId: PartyId, secretId: SecretId): Boolean = getSecrets(partyId)
    .elements
    .map(Secret::id)
    .contains(secretId)

fun Request.setUser(user: UserDetails?) {
    with(asDynamic()) {
        this.user = user
        this.isAuthenticated = user != null
    }
}
