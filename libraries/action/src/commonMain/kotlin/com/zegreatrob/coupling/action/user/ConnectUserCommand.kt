package com.zegreatrob.coupling.action.user

import com.zegreatrob.testmints.action.annotation.ActionMint

@ActionMint
data class ConnectUserCommand(val token: String) {
    fun interface Dispatcher {
        suspend fun perform(command: ConnectUserCommand): Boolean?
    }
}
