package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.json.toPinRecord
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.repository.pin.PinGet
import com.zegreatrob.coupling.sdk.TribeGQLComponent.PinList
import kotlin.js.Json

interface SdkPinGet : PinGet, GqlQueryComponent {
    override suspend fun getPins(tribeId: TribeId) = performQueryGetComponent(
        tribeId,
        PinList,
        ::doPinThing
    ) ?: emptyList()

    private fun doPinThing(content: dynamic) = content
        .unsafeCast<Array<Json>?>()
        ?.map(Json::toPinRecord)
}
