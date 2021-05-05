package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.repository.tribe.TribeDelete

interface SdkTribeDelete : TribeDelete, AxiosSyntax, GqlSyntax {
    override suspend fun delete(tribeId: TribeId) = "mutation { deleteTribe(tribeId: \"${tribeId.value}\") }"
        .performQuery()
        .data.data.deleteTribe
        .unsafeCast<Boolean>()
}
