package com.zegreatrob.coupling.action.boost

import com.zegreatrob.coupling.model.Boost
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.testmints.action.annotation.ActionMint

@ActionMint
class BoostQuery {
    interface Dispatcher {
        suspend fun perform(command: BoostQuery): Record<Boost>?
    }
}
