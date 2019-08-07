package com.zegreatrob.coupling.client.user

import com.zegreatrob.coupling.client.sdk.ServerLogout

object LogoutCommand

interface LogoutCommandDispatcher : ServerLogout {
    suspend fun LogoutCommand.perform() = logout()
}

