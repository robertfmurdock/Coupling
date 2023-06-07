package com.zegreatrob.coupling.action.boost

import com.zegreatrob.coupling.model.Boost
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.testmints.action.async.SimpleSuspendAction

class BoostQuery : SimpleSuspendAction<BoostQuery.Dispatcher, Record<Boost>?> {

    override val performFunc = link(Dispatcher::perform)

    interface Dispatcher {
        suspend fun perform(command: BoostQuery): Record<Boost>?
    }
}
