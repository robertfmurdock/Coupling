package com.zegreatrob.coupling.action.boost

import com.zegreatrob.coupling.action.VoidResult
import com.zegreatrob.testmints.action.annotation.ActionMint

@ActionMint
class DeleteBoostCommand {
    interface Dispatcher {
        suspend fun perform(command: DeleteBoostCommand): VoidResult
    }
}
