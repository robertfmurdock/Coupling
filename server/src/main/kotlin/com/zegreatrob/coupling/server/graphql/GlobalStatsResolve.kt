package com.zegreatrob.coupling.server.graphql

import com.zegreatrob.coupling.json.JsonGlobalStatsInput
import com.zegreatrob.coupling.json.toJson
import com.zegreatrob.coupling.model.GlobalStats
import com.zegreatrob.coupling.server.action.GlobalStatsQuery
import kotlinx.serialization.json.JsonNull

val globalStatsResolve = dispatch(
    dispatcherFunc = DispatcherProviders.command(),
    queryFunc = { _: JsonNull, input: JsonGlobalStatsInput -> GlobalStatsQuery(input.year) },
    toSerializable = GlobalStats::toJson,
)
