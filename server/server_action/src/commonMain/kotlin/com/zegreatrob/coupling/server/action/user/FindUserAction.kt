package com.zegreatrob.coupling.server.action.user

import com.zegreatrob.coupling.action.Action
import com.zegreatrob.coupling.action.ActionLoggingSyntax
import com.zegreatrob.coupling.model.user.User
import com.zegreatrob.coupling.model.user.UserEmailSyntax
import com.zegreatrob.coupling.repository.user.UserRepository

object FindUserAction : Action

interface FindUserActionDispatcher : ActionLoggingSyntax, UserEmailSyntax {

    val userRepository: UserRepository

    suspend fun FindUserAction.perform(): User? = logAsync { userRepository.getUser() }
}