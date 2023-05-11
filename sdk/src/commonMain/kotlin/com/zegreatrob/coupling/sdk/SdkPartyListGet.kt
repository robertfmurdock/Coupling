package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.json.JsonPartyRecord
import com.zegreatrob.coupling.json.fromJsonElement
import com.zegreatrob.coupling.json.toModelRecord
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.party.Party
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject

interface SdkPartyListGet : BarebonesSdk, GqlSyntax, GraphQueries

fun JsonElement.partyRecords() = jsonObject["data"]
    ?.jsonObject
    ?.get("partyList")
    .toPartyRecordList()

private fun JsonElement?.toPartyRecordList(): List<Record<Party>> = this?.fromJsonElement<List<JsonPartyRecord>>()
    ?.map(JsonPartyRecord::toModelRecord)
    ?: emptyList()
