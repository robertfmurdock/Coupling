package com.zegreatrob.coupling.action.boost

import com.zegreatrob.coupling.action.VoidResult
import com.zegreatrob.testmints.action.annotation.MintAction

@MintAction
class DeleteBoostCommand {
    interface Dispatcher {
        suspend fun perform(command: DeleteBoostCommand): VoidResult
    }
}
