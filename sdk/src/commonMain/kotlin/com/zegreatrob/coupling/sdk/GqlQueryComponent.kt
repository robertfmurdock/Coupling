package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.json.couplingJsonFormat
import com.zegreatrob.coupling.model.party.PartyId
import kotlinx.serialization.json.decodeFromJsonElement

interface GqlQueryComponent : PartyGQLPerformer, GraphQueries

suspend inline fun <reified T, reified S : Any> GqlQueryComponent.performQueryGetComponent(
    partyId: PartyId,
    gqlComponent: PartyGQLComponent,
    transform: (S) -> T?
): T? = performPartyGQLQuery(partyId, listOf(gqlComponent))
    .let {
        val content = it[gqlComponent]
        if (content != null)
            couplingJsonFormat.decodeFromJsonElement<S?>(content)?.let(transform)
        else
            null
    }
