package com.zegreatrob.coupling.server.entity.boost

import com.zegreatrob.coupling.action.boost.BoostQuery
import com.zegreatrob.coupling.action.boost.fire
import com.zegreatrob.coupling.json.JsonParty
import com.zegreatrob.coupling.json.toSerializable
import com.zegreatrob.coupling.model.Boost
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.server.graphql.DispatcherProviders.prereleaseCommand
import com.zegreatrob.coupling.server.graphql.dispatchAction
import kotlinx.serialization.json.JsonNull

val boostResolver = dispatchAction(
    dispatcherFunc = prereleaseCommand(),
    commandFunc = { _: JsonParty, _: JsonNull -> BoostQuery() },
    fireCommand = ::fire,
    toSerializable = ::jsonBoostRecord,
)

private fun jsonBoostRecord(record: Record<Boost>?) = record?.toSerializable()
