package com.zegreatrob.coupling.server

import com.benasher44.uuid.Uuid
import com.zegreatrob.coupling.action.DispatchingCommandExecutor
import com.zegreatrob.coupling.action.LoggingCommandExecuteSyntax
import com.zegreatrob.coupling.action.TraceIdSyntax
import com.zegreatrob.coupling.repository.user.UserRepository
import com.zegreatrob.coupling.server.action.user.FindOrCreateUserActionDispatcher

class AuthActionDispatcher internal constructor(
    override val userId: String,
    override val userRepository: UserRepository,
    override val traceId: Uuid
) : TraceIdSyntax,
    LoggingCommandExecuteSyntax,
    FindOrCreateUserActionDispatcher,
    UserRepository by userRepository,
    DispatchingCommandExecutor<AuthActionDispatcher> {
    override val actionDispatcher = this
}
