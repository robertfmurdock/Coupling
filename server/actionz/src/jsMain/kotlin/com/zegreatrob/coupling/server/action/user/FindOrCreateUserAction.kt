package com.zegreatrob.coupling.server.action.user

import com.zegreatrob.coupling.action.SimpleSuspendResultAction
import com.zegreatrob.coupling.action.successResult
import com.zegreatrob.coupling.model.user.UserDetails
import com.zegreatrob.coupling.model.user.UserIdProvider
import kotools.types.text.toNotBlankString
import kotlin.uuid.Uuid

object FindOrCreateUserAction : SimpleSuspendResultAction<FindOrCreateUserActionDispatcher, UserDetails> {
    override val performFunc = link(FindOrCreateUserActionDispatcher::perform)
}

interface FindOrCreateUserActionDispatcher :
    UserIdProvider,
    UserSaveSyntax,
    UserGetSyntax {

    suspend fun perform(action: FindOrCreateUserAction) = findOrCreateUser().successResult()

    private suspend fun findOrCreateUser() = loadUser()
        ?: getFirstUserWithEmail()
        ?: newUser()

    private suspend inline fun getFirstUserWithEmail() = userRepository.getUsersWithEmail(userId.toString())
        .firstOrNull()
        ?.data

    private suspend fun newUser() = UserDetails(
        id = "${Uuid.random()}".toNotBlankString().getOrThrow(),
        email = userId,
        authorizedPartyIds = emptySet(),
        stripeCustomerId = null,
    )
        .apply { save() }
}
