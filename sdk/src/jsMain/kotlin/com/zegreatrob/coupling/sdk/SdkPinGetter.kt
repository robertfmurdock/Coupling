package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.json.toPin
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.repository.pin.PinGetter
import com.zegreatrob.coupling.sdk.TribeGQLComponent.PinList
import kotlin.js.Json

interface SdkPinGetter : PinGetter, GqlQueryComponent {
    override suspend fun getPins(tribeId: TribeId) = performQueryGetComponent(
        tribeId,
        PinList,
        ::doPinThing
    )

    private fun doPinThing(content: dynamic) = content
        .unsafeCast<Array<Json>?>()
        ?.map(Json::toPin)
}
