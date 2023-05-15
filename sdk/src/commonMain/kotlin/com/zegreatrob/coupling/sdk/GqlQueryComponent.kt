package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.json.JsonCouplingQueryResult
import com.zegreatrob.coupling.model.party.PartyId

interface GqlQueryComponent : PartyGQLPerformer, GraphQueries

suspend inline fun <reified T> GqlQueryComponent.performQueryGetComponent(
    partyId: PartyId,
    gqlComponent: PartyGQLComponent,
    transform: (JsonCouplingQueryResult?) -> T?,
): T? = performPartyGQLQuery(partyId, listOf(gqlComponent))
    .let { content -> content?.let(transform) }
