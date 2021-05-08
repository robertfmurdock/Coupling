package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.repository.tribe.TribeDelete
import kotlin.js.json

interface SdkTribeDelete : TribeDelete, AxiosSyntax, GqlSyntax {
    override suspend fun delete(tribeId: TribeId) =
        performQuery(
            json(
                "query" to Mutations.deleteTribe,
                "variables" to json("input" to json("tribeId" to tribeId.value))
            )
        )
            .data.data.deleteTribe
            .unsafeCast<Boolean>()
}
