package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.json.toTribe
import com.zegreatrob.coupling.model.tribe.TribeGet
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.sdk.external.axios.AxiosGetEntitySyntax
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async

interface SdkGetTribe : AxiosGetEntitySyntax, TribeGet, AxiosSyntax {
    override fun CoroutineScope.getTribeAsync(tribeId: TribeId) = async {
        axios.getEntityAsync("/api/tribes/${tribeId.value}")
            .await()
            .toTribe()
    }
}