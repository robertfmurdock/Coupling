package com.zegreatrob.coupling.server.entity.contribution

import com.zegreatrob.coupling.json.GqlContributionReport
import com.zegreatrob.coupling.json.toJson
import com.zegreatrob.coupling.json.toModel
import com.zegreatrob.coupling.model.Contributor
import com.zegreatrob.coupling.model.elements
import com.zegreatrob.coupling.server.action.contribution.PartyContributorQuery
import com.zegreatrob.coupling.server.action.contribution.perform
import com.zegreatrob.coupling.server.express.route.CouplingContext
import com.zegreatrob.coupling.server.graphql.dispatch
import kotlinx.serialization.json.JsonNull

val partyContributorResolver = dispatch(
    dispatcherFunc = { context: CouplingContext, _: GqlContributionReport, _: JsonNull? ->
        context.commandDispatcher
    },
    commandFunc = { data, _: JsonNull? ->
        val report = data.toModel()
        PartyContributorQuery(
            report.partyId ?: return@dispatch null,
            report.contributions?.elements ?: return@dispatch null,
        )
    },
    fireFunc = ::perform,
    toSerializable = { it.map(Contributor::toJson) },
)
