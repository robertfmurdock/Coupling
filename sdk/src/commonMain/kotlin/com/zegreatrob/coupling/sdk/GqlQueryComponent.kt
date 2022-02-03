package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.json.couplingJsonFormat
import com.zegreatrob.coupling.model.tribe.TribeId
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.decodeFromJsonElement

interface GqlQueryComponent : TribeGQLPerformer

@OptIn(ExperimentalSerializationApi::class)
suspend inline fun <reified T, reified S> GqlQueryComponent.performQueryGetComponent(
    tribeId: TribeId,
    gqlComponent: TribeGQLComponent,
    transform: (S) -> T?
): T? = performTribeGQLQuery(tribeId, listOf(gqlComponent))
    .let {
        val content = it[gqlComponent]
        if (content != null)
            transform(couplingJsonFormat.decodeFromJsonElement(content))
        else
            null
    }
