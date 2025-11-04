package com.zegreatrob.coupling.server.entity.party

import com.zegreatrob.coupling.json.GqlAccessType
import com.zegreatrob.coupling.server.action.party.PartyListQuery
import com.zegreatrob.coupling.server.action.party.PartyListResult
import com.zegreatrob.coupling.server.action.party.perform
import com.zegreatrob.coupling.server.graphql.DispatcherProviders.command
import com.zegreatrob.coupling.server.graphql.GqlPartyNode
import com.zegreatrob.coupling.server.graphql.dispatch
import kotlinx.serialization.json.JsonNull

val partyListResolve = dispatch(
    dispatcherFunc = command(),
    commandFunc = { _: JsonNull, _: JsonNull? -> PartyListQuery },
    fireFunc = ::perform,
    toSerializable = ::toJson,
)

private fun toJson(records: PartyListResult) = records.ownedParties.map { GqlPartyNode(id = it.data.id, accessType = GqlAccessType.Owner) }
    .plus(records.playerParties.map { GqlPartyNode(id = it.data.id, accessType = GqlAccessType.Player) })
