package com.zegreatrob.coupling.server.graphql

import com.zegreatrob.coupling.json.JsonGlobalStatsInput
import com.zegreatrob.coupling.json.toJson
import com.zegreatrob.coupling.model.GlobalStats
import com.zegreatrob.coupling.server.action.GlobalStatsQuery
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement

val globalStatsResolve = dispatch(
    dispatcherFunc = DispatcherProviders.command,
    queryFunc = { _, input: JsonElement ->
        val parsedInput = Json.decodeFromJsonElement<JsonGlobalStatsInput>(input)
        GlobalStatsQuery(parsedInput.year)
    },
    toSerializable = GlobalStats::toJson,
)
