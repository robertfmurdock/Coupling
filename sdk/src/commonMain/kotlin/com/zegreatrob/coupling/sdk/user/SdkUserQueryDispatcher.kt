package com.zegreatrob.coupling.sdk.user

import com.zegreatrob.coupling.action.user.UserQuery
import com.zegreatrob.coupling.json.JsonUser
import com.zegreatrob.coupling.json.fromJsonElement
import com.zegreatrob.coupling.json.toModel
import com.zegreatrob.coupling.model.user.User
import com.zegreatrob.coupling.sdk.GqlSyntax
import com.zegreatrob.coupling.sdk.GraphQueries
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.put

interface SdkUserQueryDispatcher : UserQuery.Dispatcher, GqlSyntax, GraphQueries {
    override suspend fun perform(query: UserQuery): User? =
        performer.postAsync(buildJsonObject { put("query", queries.user) }).await()
            .jsonObject["data"]!!
            .jsonObject["user"]!!
            .fromJsonElement<JsonUser>()
            .toModel()
}
