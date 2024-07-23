package com.zegreatrob.coupling.server.entity.contribution

import com.zegreatrob.coupling.json.toJson
import com.zegreatrob.coupling.model.Contributor
import com.zegreatrob.coupling.model.party.PartyElement
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.server.action.contribution.PartyContributorQuery
import com.zegreatrob.coupling.server.action.contribution.perform
import com.zegreatrob.coupling.server.graphql.DispatcherProviders
import com.zegreatrob.coupling.server.graphql.dispatch
import kotlinx.serialization.json.JsonNull

val partyContributorResolver = dispatch(
    dispatcherFunc = DispatcherProviders.partyCommand,
    commandFunc = { data, _: JsonNull? -> data.id?.let(::PartyId)?.let { PartyContributorQuery(it) } },
    fireFunc = ::perform,
    toSerializable = { it.map(PartyElement<Contributor>::toJson) },
)
