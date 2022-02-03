package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.repository.tribe.TribeDelete

interface SdkTribeDelete : TribeDelete, GqlSyntax {
    override suspend fun delete(tribeId: TribeId): Boolean = doQuery(
        Mutations.deleteTribe,
        mapOf("tribeId" to tribeId.value),
        "deleteTribe"
    ) { it: Boolean? -> it } ?: false
}
