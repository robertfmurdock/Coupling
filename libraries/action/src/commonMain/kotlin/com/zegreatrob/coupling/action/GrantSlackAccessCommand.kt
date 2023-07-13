package com.zegreatrob.coupling.action

import com.zegreatrob.testmints.action.annotation.MintAction

@MintAction
data class GrantSlackAccessCommand(val code: String, val state: String) {
    interface Dispatcher {
        suspend fun perform(command: GrantSlackAccessCommand): VoidResult
    }
}
