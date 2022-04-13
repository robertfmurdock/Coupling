package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.json.JsonTribeRecord
import com.zegreatrob.coupling.json.fromJsonElement
import com.zegreatrob.coupling.json.toModelRecord
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.coupling.repository.party.PartyListGet
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.put

interface SdkPartyListGet : PartyListGet, GqlSyntax, GraphQueries {
    override suspend fun getParties() = performer.postAsync(tribeListQuery()).await()
        .jsonObject["data"]
        ?.jsonObject?.get("tribeList")
        .toTribeRecordList()

    private fun tribeListQuery() = buildJsonObject { put("query", queries.listTribes) }

    private fun JsonElement?.toTribeRecordList(): List<Record<Party>> = this?.fromJsonElement<List<JsonTribeRecord>>()
        ?.map(JsonTribeRecord::toModelRecord)
        ?: emptyList()
}
