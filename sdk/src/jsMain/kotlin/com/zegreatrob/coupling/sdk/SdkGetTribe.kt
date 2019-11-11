package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.json.toTribe
import com.zegreatrob.coupling.model.tribe.KtTribe
import com.zegreatrob.coupling.model.tribe.TribeGet
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.sdk.external.axios.AxiosGetEntitySyntax

interface SdkGetTribe : AxiosGetEntitySyntax, TribeGet, AxiosSyntax {
    override suspend fun getTribe(tribeId: TribeId): KtTribe? =
        axios.getEntityAsync("/api/tribes/${tribeId.value}")
            .await()
            .toTribe()
}