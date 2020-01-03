package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.json.toTribe
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.repository.tribe.TribeGet
import com.zegreatrob.coupling.sdk.TribeGQLComponent.TribeData
import kotlin.js.Json

interface SdkTribeGet : TribeGet, GqlQueryComponent {
    override suspend fun getTribe(tribeId: TribeId) = performQueryGetComponent(tribeId, TribeData) {
        it.unsafeCast<Json?>()
            ?.toTribe()
    }
}