package com.zegreatrob.coupling.server.entity.boost

import com.zegreatrob.coupling.json.toSerializable
import com.zegreatrob.coupling.server.action.boost.BoostQuery
import com.zegreatrob.coupling.server.graphql.DispatcherProviders.prereleaseCommand
import com.zegreatrob.coupling.server.graphql.dispatch
import kotlinx.serialization.json.JsonElement

val boostResolver = dispatch(
    prereleaseCommand,
    { _, _: JsonElement -> BoostQuery() },
    {
        println("boost result $it")


        val toSerializable = it?.toSerializable()
        println("boost toSerializable $toSerializable")

        toSerializable
    }
)
