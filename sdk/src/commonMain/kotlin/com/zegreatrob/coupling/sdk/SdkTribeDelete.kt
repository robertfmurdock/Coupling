package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.repository.tribe.TribeDelete

interface SdkTribeDelete : TribeDelete, GqlSyntax, GraphQueries {
    override suspend fun delete(tribeId: TribeId): Boolean = doQuery(
        mutations.deleteTribe,
        mapOf("tribeId" to tribeId.value),
        "deleteTribe"
    ) { it: Boolean? -> it } ?: false
}
