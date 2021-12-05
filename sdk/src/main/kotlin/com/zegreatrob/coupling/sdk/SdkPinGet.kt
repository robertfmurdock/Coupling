package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.json.JsonPinRecord
import com.zegreatrob.coupling.json.toModelRecord
import com.zegreatrob.coupling.json.toModel
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.repository.pin.PinGet
import com.zegreatrob.coupling.sdk.TribeGQLComponent.PinList

interface SdkPinGet : PinGet, GqlQueryComponent {
    override suspend fun getPins(tribeId: TribeId) = performQueryGetComponent(tribeId, PinList, ::toModel)
        ?: emptyList()

    private fun toModel(content: List<JsonPinRecord>) = content.map(JsonPinRecord::toModel)
}
