package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.json.toTribe
import com.zegreatrob.coupling.model.tribe.TribeGet
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.sdk.external.axios.AxiosGetEntitySyntax
import kotlinx.coroutines.asDeferred
import kotlin.js.Json

interface SdkGetTribe : AxiosGetEntitySyntax, TribeGet, AxiosSyntax {
    override fun getTribeAsync(tribeId: TribeId) = axios.getEntityAsync("/api/tribes/${tribeId.value}")
        .then(Json::toTribe)
        .asDeferred()
}