package com.zegreatrob.coupling.server.entity.contribution

import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.server.action.contribution.PartyContributionQuery
import com.zegreatrob.coupling.server.action.contribution.perform
import com.zegreatrob.coupling.server.graphql.DispatcherProviders
import com.zegreatrob.coupling.server.graphql.dispatch
import kotlinx.serialization.json.JsonNull

val partyContributionStatisticsResolver = dispatch(
    dispatcherFunc = DispatcherProviders.partyCommand,
    commandFunc = { data, _: JsonNull? -> data.id?.let(::PartyId)?.let { PartyContributionQuery(it, null, null) } },
    fireFunc = ::perform,
    toSerializable = toSerializableContributionStatistics,
)
