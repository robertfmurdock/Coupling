package com.zegreatrob.coupling.action

import com.zegreatrob.testmints.action.annotation.ActionMint

@ActionMint
data class GrantSlackAccessCommand(val code: String, val state: String) {
    fun interface Dispatcher {
        suspend fun perform(command: GrantSlackAccessCommand): VoidResult
    }
}
