package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.json.JsonPartyRecord
import com.zegreatrob.coupling.json.fromJsonElement
import com.zegreatrob.coupling.json.toModelRecord
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.party.Party
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.put

interface SdkPartyListGet : GqlSyntax, GraphQueries {
    suspend fun getParties() = performer.postAsync(partyListQuery()).await()
        .jsonObject["data"]
        ?.jsonObject?.get("partyList")
        .toPartyRecordList()

    private fun partyListQuery() = buildJsonObject { put("query", queries.listParties) }

    private fun JsonElement?.toPartyRecordList(): List<Record<Party>> = this?.fromJsonElement<List<JsonPartyRecord>>()
        ?.map(JsonPartyRecord::toModelRecord)
        ?: emptyList()
}
