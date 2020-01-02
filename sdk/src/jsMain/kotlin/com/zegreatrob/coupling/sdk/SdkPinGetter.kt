package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.json.toPin
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.repository.pin.PinGetter
import com.zegreatrob.coupling.sdk.TribeGQLComponent.PinList
import kotlin.js.Json

interface SdkPinGetter : PinGetter, TribeGQLSyntax {
    override suspend fun getPins(tribeId: TribeId): List<Pin> = performTribeGQLQuery(tribeId, listOf(PinList))
        .let {
            it[PinList].unsafeCast<Array<Json>?>()
                ?.map(Json::toPin)
        }
        .let { it ?: throw Exception("Tribe not found.") }
}