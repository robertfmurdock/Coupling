package com.zegreatrob.coupling.server

import com.benasher44.uuid.Uuid
import com.zegreatrob.coupling.action.DispatchingActionExecutor
import com.zegreatrob.coupling.action.LoggingActionExecuteSyntax
import com.zegreatrob.coupling.action.TraceIdSyntax
import com.zegreatrob.coupling.repository.user.UserRepository
import com.zegreatrob.coupling.server.action.user.FindOrCreateUserActionDispatcher

class AuthActionDispatcher internal constructor(
    override val userId: String,
    override val userRepository: UserRepository,
    override val traceId: Uuid
) : TraceIdSyntax,
    LoggingActionExecuteSyntax,
    FindOrCreateUserActionDispatcher,
    UserRepository by userRepository,
    DispatchingActionExecutor<AuthActionDispatcher> {
    override val actionDispatcher = this
}
