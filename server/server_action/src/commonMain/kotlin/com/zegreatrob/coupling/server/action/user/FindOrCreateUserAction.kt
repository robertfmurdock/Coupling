package com.zegreatrob.coupling.server.action.user

import com.benasher44.uuid.uuid4
import com.zegreatrob.coupling.action.Action
import com.zegreatrob.coupling.action.ActionLoggingSyntax
import com.zegreatrob.coupling.model.user.User
import com.zegreatrob.coupling.model.user.UserEmailSyntax

object FindOrCreateUserAction : Action

interface FindOrCreateUserActionDispatcher : ActionLoggingSyntax, UserEmailSyntax, UserSaveSyntax, UserGetSyntax {

    suspend fun FindOrCreateUserAction.perform(): User = logAsync { loadUser() ?: newUser() }

    private suspend fun newUser() = User(id = "${uuid4()}", email = userEmail, authorizedTribeIds = emptySet())
        .apply { save() }
}
