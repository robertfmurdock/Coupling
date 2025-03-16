package com.zegreatrob.coupling.server.action.user

import com.zegreatrob.coupling.action.SimpleSuspendResultAction
import com.zegreatrob.coupling.action.successResult
import com.zegreatrob.coupling.model.user.UserDetails
import com.zegreatrob.coupling.model.user.UserId
import com.zegreatrob.coupling.model.user.UserIdProvider

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

    private suspend inline fun getFirstUserWithEmail() = userRepository.getUsersWithEmail(userId.value)
        .firstOrNull()
        ?.data

    private suspend fun newUser() = UserDetails(
        id = UserId.new(),
        email = userId.value,
        authorizedPartyIds = emptySet(),
        stripeCustomerId = null,
    )
        .apply { save() }
}
