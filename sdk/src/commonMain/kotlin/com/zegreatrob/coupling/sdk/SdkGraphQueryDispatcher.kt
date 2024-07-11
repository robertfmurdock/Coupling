package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.sdk.gql.GqlTrait
import com.zegreatrob.coupling.sdk.gql.GraphQuery
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

interface SdkGraphQueryDispatcher :
    GraphQuery.Dispatcher,
    GqlTrait {

    override suspend fun perform(query: GraphQuery) = query.postBody().perform()

    private fun GraphQuery.postBody() = buildJsonObject {
        put("query", queryString)
        variables?.let { put("variables", it) }
    }
}
