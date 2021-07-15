package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.json.JsonTribe
import com.zegreatrob.coupling.json.couplingJsonFormat
import com.zegreatrob.coupling.json.toModelRecord
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.repository.tribe.TribeGet
import com.zegreatrob.coupling.sdk.TribeGQLComponent.TribeData
import kotlinx.serialization.json.decodeFromDynamic

interface SdkTribeGet : TribeGet, GqlQueryComponent {
    override suspend fun getTribeRecord(tribeId: TribeId) = performQueryGetComponent(tribeId, TribeData) {
        couplingJsonFormat.decodeFromDynamic<JsonTribe>(it).toModelRecord()
    }
}