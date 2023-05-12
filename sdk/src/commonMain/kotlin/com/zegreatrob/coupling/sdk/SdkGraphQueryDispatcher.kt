package com.zegreatrob.coupling.sdk

interface SdkGraphQueryDispatcher : GraphQuery.Dispatcher, GqlSyntax {
    override suspend fun perform(query: GraphQuery) = query.queryString.perform()
}
