package com.zegreatrob.coupling.server.graphql

import com.zegreatrob.coupling.json.JsonGlobalStatsInput
import com.zegreatrob.coupling.json.toJson
import com.zegreatrob.coupling.model.GlobalStats
import com.zegreatrob.coupling.server.action.GlobalStatsQuery
import com.zegreatrob.coupling.server.action.perform
import com.zegreatrob.coupling.server.entity.boost.requiredInput
import kotlinx.serialization.json.JsonNull

val globalStatsResolve = dispatch(
    dispatcherFunc = DispatcherProviders.command(),
    commandFunc = requiredInput { _: JsonNull, input: JsonGlobalStatsInput -> GlobalStatsQuery(input.year) },
    fireFunc = ::perform,
    toSerializable = GlobalStats::toJson,
)
