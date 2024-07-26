package com.zegreatrob.coupling.server.entity.boost

import com.zegreatrob.coupling.action.boost.PartyBoostQuery
import com.zegreatrob.coupling.action.boost.UserBoostQuery
import com.zegreatrob.coupling.action.boost.perform
import com.zegreatrob.coupling.json.GqlParty
import com.zegreatrob.coupling.json.toSerializable
import com.zegreatrob.coupling.model.Boost
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.server.graphql.DispatcherProviders.prereleaseCommand
import com.zegreatrob.coupling.server.graphql.dispatch
import kotlinx.serialization.json.JsonNull

val userBoostResolver = dispatch(
    dispatcherFunc = prereleaseCommand(),
    commandFunc = { _: JsonNull, _: JsonNull? -> UserBoostQuery() },
    fireFunc = ::perform,
    toSerializable = ::jsonBoostRecord,
)

val partyBoostResolver = dispatch(
    dispatcherFunc = prereleaseCommand(),
    commandFunc = { partyJson: GqlParty, _: JsonNull? -> partyJson.id?.let { PartyBoostQuery(PartyId(it)) } },
    fireFunc = ::perform,
    toSerializable = ::jsonBoostRecord,
)

private fun jsonBoostRecord(record: Record<Boost>?) = record?.toSerializable()
