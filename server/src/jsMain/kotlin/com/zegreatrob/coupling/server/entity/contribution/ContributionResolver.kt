package com.zegreatrob.coupling.server.entity.contribution

import com.zegreatrob.coupling.json.ContributionsInput
import com.zegreatrob.coupling.json.JsonPair
import com.zegreatrob.coupling.json.JsonParty
import com.zegreatrob.coupling.json.toJson
import com.zegreatrob.coupling.json.toModel
import com.zegreatrob.coupling.model.Contribution
import com.zegreatrob.coupling.model.Contributor
import com.zegreatrob.coupling.model.PartyRecord
import com.zegreatrob.coupling.model.elements
import com.zegreatrob.coupling.model.pairassignmentdocument.toCouplingPair
import com.zegreatrob.coupling.model.party.PartyElement
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.server.action.contribution.PairContributionQuery
import com.zegreatrob.coupling.server.action.contribution.PartyContributionQuery
import com.zegreatrob.coupling.server.action.contribution.PartyContributorQuery
import com.zegreatrob.coupling.server.action.contribution.perform
import com.zegreatrob.coupling.server.express.route.CouplingContext
import com.zegreatrob.coupling.server.graphql.DispatcherProviders.partyCommand
import com.zegreatrob.coupling.server.graphql.dispatch
import kotlinx.serialization.json.JsonNull

val contributionResolver = dispatch(
    dispatcherFunc = { context: CouplingContext, _: JsonParty, _: ContributionsInput? ->
        context.commandDispatcher
    },
    commandFunc = { data, input: ContributionsInput? ->
        data.id?.let(::PartyId)?.let { PartyContributionQuery(it, input?.window?.toModel()) }
    },
    fireFunc = ::perform,
    toSerializable = { it.map(PartyRecord<Contribution>::toJson) },
)

val contributorResolver = dispatch(
    dispatcherFunc = partyCommand,
    commandFunc = { data, _: JsonNull? -> data.id?.let(::PartyId)?.let { PartyContributorQuery(it) } },
    fireFunc = ::perform,
    toSerializable = { it.map(PartyElement<Contributor>::toJson) },
)

val pairContributionResolver = dispatch(
    dispatcherFunc = { context: CouplingContext, _: JsonPair, _: ContributionsInput? -> context.commandDispatcher },
    commandFunc = { data, contributions ->
        val model = data.toModel()
        val partyId = PartyId(data.partyId ?: return@dispatch null)
        val players = model.players?.elements ?: return@dispatch null
        PairContributionQuery(
            partyId = partyId,
            pair = players.toCouplingPair(),
            window = contributions?.window?.toModel(),
        )
    },
    fireFunc = ::perform,
    toSerializable = { it.map(PartyRecord<Contribution>::toJson) },
)
