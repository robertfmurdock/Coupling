package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.json.JsonTribeRecord
import com.zegreatrob.coupling.json.toModelRecord
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.repository.tribe.TribeGet
import com.zegreatrob.coupling.sdk.TribeGQLComponent.TribeData

interface SdkTribeGet : TribeGet, GqlQueryComponent {
    override suspend fun getTribeRecord(tribeId: TribeId) = performQueryGetComponent(
        tribeId,
        TribeData,
        JsonTribeRecord::toModelRecord
    )
}
