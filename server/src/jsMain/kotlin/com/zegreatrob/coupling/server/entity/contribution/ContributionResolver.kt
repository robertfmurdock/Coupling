package com.zegreatrob.coupling.server.entity.contribution

import com.zegreatrob.coupling.json.ContributionsInput
import com.zegreatrob.coupling.json.JsonParty
import com.zegreatrob.coupling.json.toJson
import com.zegreatrob.coupling.json.toModel
import com.zegreatrob.coupling.model.Contribution
import com.zegreatrob.coupling.model.PartyRecord
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.server.action.contribution.PartyContributionQuery
import com.zegreatrob.coupling.server.action.contribution.perform
import com.zegreatrob.coupling.server.express.route.CouplingContext
import com.zegreatrob.coupling.server.graphql.dispatch

val contributionResolver = dispatch(
    dispatcherFunc = { context: CouplingContext, _: JsonParty, _: ContributionsInput? ->
        context.commandDispatcher
    },
    commandFunc = { data, input: ContributionsInput? ->
        data.id?.let(::PartyId)?.let { PartyContributionQuery(it, input?.window?.toModel(), input?.limit) }
    },
    fireFunc = ::perform,
    toSerializable = { it.map(PartyRecord<Contribution>::toJson) },
)
