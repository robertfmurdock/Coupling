package com.zegreatrob.coupling.action.boost

import com.zegreatrob.coupling.action.Result
import com.zegreatrob.coupling.action.SimpleSuspendResultAction
import com.zegreatrob.coupling.model.Boost
import com.zegreatrob.coupling.model.Record

class BoostQuery : SimpleSuspendResultAction<BoostQuery.Dispatcher, Record<Boost>> {
    override val performFunc = link(Dispatcher::perform)

    interface Dispatcher {
        suspend fun perform(command: BoostQuery): Result<Record<Boost>>
    }
}
