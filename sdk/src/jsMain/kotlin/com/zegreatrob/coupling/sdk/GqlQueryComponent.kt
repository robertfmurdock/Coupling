package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.model.tribe.TribeId

interface GqlQueryComponent : TribeGQLPerformer {
    suspend fun <T> performQueryGetComponent(
        tribeId: TribeId,
        gqlComponent: TribeGQLComponent,
        transform: (dynamic) -> T?
    ): T? = performTribeGQLQuery(tribeId, listOf(gqlComponent))
        .let {
            val content = it[gqlComponent]
            transform(content)
        }
}