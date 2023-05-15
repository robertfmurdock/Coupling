package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.model.party.PartyId

interface GqlQueryComponent : PartyGQLPerformer, GraphQueries

suspend inline fun GqlQueryComponent.performQueryGetComponent(partyId: PartyId, gqlComponent: PartyGQLComponent) =
    performPartyGQLQuery(partyId, listOf(gqlComponent))
