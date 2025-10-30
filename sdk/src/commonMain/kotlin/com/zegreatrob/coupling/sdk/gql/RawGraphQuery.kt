package com.zegreatrob.coupling.sdk.gql

import com.zegreatrob.coupling.model.CouplingQueryResult
import com.zegreatrob.coupling.sdk.dsl.CouplingQueryBuilder
import com.zegreatrob.testmints.action.async.SimpleSuspendAction
import kotlinx.serialization.json.JsonObject

data class RawGraphQuery(val queryString: String, val variables: JsonObject?) : SimpleSuspendAction<RawGraphQuery.Dispatcher, CouplingQueryResult?> {
    override val performFunc = link(Dispatcher::perform)

    interface Dispatcher {
        suspend fun perform(query: RawGraphQuery): CouplingQueryResult?
    }
}

fun graphQuery(block: CouplingQueryBuilder.() -> Unit) = CouplingQueryBuilder()
    .apply(block)
    .build()
    .let { (query, variables) -> RawGraphQuery(query, variables) }
