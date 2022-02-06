package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.json.couplingJsonFormat
import com.zegreatrob.coupling.model.tribe.TribeId
import kotlinx.serialization.json.decodeFromJsonElement

interface GqlQueryComponent : TribeGQLPerformer, GraphQueries

suspend inline fun <reified T, reified S : Any> GqlQueryComponent.performQueryGetComponent(
    tribeId: TribeId,
    gqlComponent: TribeGQLComponent,
    transform: (S) -> T?
): T? = performTribeGQLQuery(tribeId, listOf(gqlComponent))
    .let {
        val content = it[gqlComponent]
        if (content != null)
            couplingJsonFormat.decodeFromJsonElement<S?>(content)?.let(transform)
        else
            null
    }
