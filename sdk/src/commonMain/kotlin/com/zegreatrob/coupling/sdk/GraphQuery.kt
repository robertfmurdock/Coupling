package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.json.CouplingQueryResult
import com.zegreatrob.testmints.action.async.SimpleSuspendAction

data class GraphQuery(val queryString: String) : SimpleSuspendAction<GraphQuery.Dispatcher, CouplingQueryResult?> {
    override val performFunc = link(Dispatcher::perform)

    interface Dispatcher {
        suspend fun perform(query: GraphQuery): CouplingQueryResult?
    }
}
