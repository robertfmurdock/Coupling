package com.zegreatrob.coupling.server.entity.contribution

import com.zegreatrob.coupling.json.GqlContributionsInput
import com.zegreatrob.coupling.json.toJson
import com.zegreatrob.coupling.server.action.contribution.PartyContributionQuery
import com.zegreatrob.coupling.server.action.contribution.perform
import com.zegreatrob.coupling.server.express.route.CouplingContext
import com.zegreatrob.coupling.server.graphql.GqlPartyNode
import com.zegreatrob.coupling.server.graphql.dispatch

val partyContributionReportResolver = dispatch(
    dispatcherFunc = { context: CouplingContext, _: GqlPartyNode, _: GqlContributionsInput? ->
        context.commandDispatcher
    },
    commandFunc = { data, input: GqlContributionsInput? ->
        PartyContributionQuery(data.id, input?.window?.toModel(), input?.limit)
    },
    fireFunc = ::perform,
    toSerializable = { it.toJson() },
)
