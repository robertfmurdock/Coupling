package com.zegreatrob.coupling.server.entity.user

import com.zegreatrob.coupling.UserEmailSyntax
import com.zegreatrob.coupling.common.Action
import com.zegreatrob.coupling.common.ActionLoggingSyntax
import com.zegreatrob.coupling.server.entity.user.User
import com.zegreatrob.coupling.server.entity.user.UserRepository

object FindUserAction : Action

interface FindUserActionDispatcher : ActionLoggingSyntax, UserEmailSyntax {

    val userRepository: UserRepository

    suspend fun FindUserAction.perform(): User? = logAsync { userRepository.getUser() }
}