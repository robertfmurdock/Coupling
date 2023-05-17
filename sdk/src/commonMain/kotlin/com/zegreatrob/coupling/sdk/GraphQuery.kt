package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.json.CouplingQueryResult
import com.zegreatrob.testmints.action.async.SimpleSuspendAction
import kotlinx.serialization.json.JsonObject

data class GraphQuery(val queryString: String, val variables: JsonObject?) :
    SimpleSuspendAction<GraphQuery.Dispatcher, CouplingQueryResult?> {
    override val performFunc = link(Dispatcher::perform)

    interface Dispatcher {
        suspend fun perform(query: GraphQuery): CouplingQueryResult?
    }
}

fun graphQuery(block: CouplingQueryBuilder.() -> Unit) =
    CouplingQueryBuilder()
        .apply(block)
        .build()
        .let { (query, variables) -> GraphQuery(query, variables) }
