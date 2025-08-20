package com.zegreatrob.coupling.server.action.user

import com.zegreatrob.coupling.action.user.ConnectUserCommand

interface ServerConnectUserCommandDispatcher : ConnectUserCommand.Dispatcher {
    override suspend fun perform(command: ConnectUserCommand): Boolean? = false
}
