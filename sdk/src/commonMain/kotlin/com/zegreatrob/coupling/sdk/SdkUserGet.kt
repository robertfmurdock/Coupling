package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.json.JsonUser
import com.zegreatrob.coupling.json.fromJsonElement
import com.zegreatrob.coupling.json.toModel
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.user.User
import com.zegreatrob.coupling.repository.user.UserGet
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.put

interface SdkUserGet : UserGet, GqlSyntax {

    override suspend fun getUser() = performer.postAsync(buildJsonObject { put("query", Queries.user) }).await()
        .jsonObject["data"]!!
        .jsonObject["user"]!!
        .toTribeRecordList()
        .let { Record(it, "") }

    private fun JsonElement.toTribeRecordList(): User = fromJsonElement<JsonUser>().toModel()

}
