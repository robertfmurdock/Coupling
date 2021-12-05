package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.json.JsonTribeRecord
import com.zegreatrob.coupling.json.couplingJsonFormat
import com.zegreatrob.coupling.json.toModelRecord
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.repository.tribe.TribeListGet
import com.zegreatrob.minjson.at
import kotlinx.serialization.json.decodeFromDynamic
import kotlin.js.Json
import kotlin.js.json

interface SdkTribeListGet : TribeListGet, AxiosSyntax, GqlSyntax {
    override suspend fun getTribes() = axios.postAsync<Json>(gqlEndpoint, tribeListQuery()).await()
        .at<Json>("/data/tribeList")
        .toTribeRecordList()

    private fun tribeListQuery() = json("query" to Queries.listTribes)

    private fun Json?.toTribeRecordList(): List<Record<Tribe>> =
        couplingJsonFormat.decodeFromDynamic<List<JsonTribeRecord>>(this)
            .map(JsonTribeRecord::toModelRecord)
}
