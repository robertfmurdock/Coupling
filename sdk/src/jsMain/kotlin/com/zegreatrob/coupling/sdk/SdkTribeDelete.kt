package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.repository.tribe.TribeDelete

interface SdkTribeDelete : TribeDelete, AxiosSyntax, GqlSyntax {
    override suspend fun delete(tribeId: TribeId): Boolean = performQuery(
        Mutations.deleteTribe,
        mapOf("tribeId" to tribeId.value),
        "deleteTribe"
    ) { it: Boolean -> it } ?: false
}
