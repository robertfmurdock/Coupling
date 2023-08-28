package com.zegreatrob.coupling.server.entity.contribution

import com.zegreatrob.coupling.json.toJson
import com.zegreatrob.coupling.model.Contribution
import com.zegreatrob.coupling.model.PartyRecord
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.server.action.contribution.PartyContributionQuery
import com.zegreatrob.coupling.server.action.contribution.perform
import com.zegreatrob.coupling.server.graphql.DispatcherProviders.partyCommand
import com.zegreatrob.coupling.server.graphql.dispatch
import kotlinx.serialization.json.JsonNull

val contributionResolver = dispatch(
    dispatcherFunc = partyCommand,
    commandFunc = { data, _: JsonNull -> data.id?.let(::PartyId)?.let { PartyContributionQuery(it) } },
    fireFunc = ::perform,
    toSerializable = { it.map(PartyRecord<Contribution>::toJson) },
)
