package com.zegreatrob.coupling.action.user

import com.zegreatrob.coupling.action.VoidResult
import com.zegreatrob.testmints.action.annotation.ActionMint
import kotools.types.text.NotBlankString

@ActionMint
data class DisconnectUserCommand(val email: NotBlankString) {
    fun interface Dispatcher {
        suspend fun perform(command: DisconnectUserCommand): VoidResult
    }
}
