package com.zegreatrob.coupling.client.user

import com.zegreatrob.coupling.client.sdk.SdkSyntax

object LogoutCommand

interface LogoutCommandDispatcher : SdkSyntax {
    suspend fun LogoutCommand.perform() = with(sdk) { logout() }
}

