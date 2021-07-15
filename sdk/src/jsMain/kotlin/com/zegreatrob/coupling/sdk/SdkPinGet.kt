package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.json.JsonPinRecord
import com.zegreatrob.coupling.json.couplingJsonFormat
import com.zegreatrob.coupling.json.toModelRecord
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.repository.pin.PinGet
import com.zegreatrob.coupling.sdk.TribeGQLComponent.PinList
import kotlinx.serialization.json.decodeFromDynamic

interface SdkPinGet : PinGet, GqlQueryComponent {
    override suspend fun getPins(tribeId: TribeId) = performQueryGetComponent(
        tribeId,
        PinList,
        ::doPinThing
    )?.map(JsonPinRecord::toModelRecord) ?: emptyList()

    private fun doPinThing(content: dynamic) = couplingJsonFormat.decodeFromDynamic<List<JsonPinRecord>>(content)

}
