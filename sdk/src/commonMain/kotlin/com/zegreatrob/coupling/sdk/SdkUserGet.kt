package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.json.JsonUser
import com.zegreatrob.coupling.json.couplingJsonFormat
import com.zegreatrob.coupling.json.toModel
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.user.User
import com.zegreatrob.coupling.repository.user.UserGet
import com.zegreatrob.minjson.at
import kotlinx.serialization.json.decodeFromDynamic
import kotlin.js.Json
import kotlin.js.json

interface SdkUserGet : UserGet, GqlSyntax {

    override suspend fun getUser() = performer.postAsync(json("query" to Queries.user)).await()
        .at<Json>("/data/user")
        .toTribeRecordList()
        .let { Record(it, "") }

    private fun Json?.toTribeRecordList(): User =
        couplingJsonFormat.decodeFromDynamic<JsonUser>(this).toModel()

}
