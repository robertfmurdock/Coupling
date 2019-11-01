package com.zegreatrob.coupling.server.action.user

import com.zegreatrob.coupling.action.UserEmailSyntax
import com.zegreatrob.coupling.action.Action
import com.zegreatrob.coupling.action.ActionLoggingSyntax
import com.zegreatrob.coupling.model.User
import com.zegreatrob.coupling.model.user.UserRepository

object FindUserAction : Action

interface FindUserActionDispatcher : ActionLoggingSyntax, UserEmailSyntax {

    val userRepository: UserRepository

    suspend fun FindUserAction.perform(): User? = logAsync { userRepository.getUser() }
}