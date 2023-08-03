package com.zegreatrob.coupling.server.action.user

import com.benasher44.uuid.uuid4
import com.zegreatrob.coupling.action.SimpleSuspendResultAction
import com.zegreatrob.coupling.action.successResult
import com.zegreatrob.coupling.model.user.User
import com.zegreatrob.coupling.model.user.UserIdProvider

object FindOrCreateUserAction : SimpleSuspendResultAction<FindOrCreateUserActionDispatcher, User> {
    override val performFunc = link(FindOrCreateUserActionDispatcher::perform)
}

interface FindOrCreateUserActionDispatcher : UserIdProvider, UserSaveSyntax, UserGetSyntax {

    suspend fun perform(action: FindOrCreateUserAction) = findOrCreateUser().successResult()

    private suspend fun findOrCreateUser() = loadUser()
        ?: getFirstUserWithEmail()
        ?: newUser()

    private suspend inline fun getFirstUserWithEmail() = userRepository.getUsersWithEmail(userId)
        .firstOrNull()
        ?.data

    private suspend fun newUser() = User(
        id = "${uuid4()}",
        email = userId,
        authorizedPartyIds = emptySet(),
        stripeCustomerId = null,
    )
        .apply { save() }
}
