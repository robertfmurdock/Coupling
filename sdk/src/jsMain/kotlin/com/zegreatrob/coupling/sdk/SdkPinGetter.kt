package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.json.toPin
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.repository.pin.PinGetter
import com.zegreatrob.coupling.sdk.TribeGQLComponent.PinList
import kotlinx.coroutines.await
import kotlin.js.Json

interface SdkPinGetter : PinGetter, TribeGQLSyntax {
    override suspend fun getPins(tribeId: TribeId): List<Pin> = performTribeGQLQuery(tribeId, listOf(PinList))
        .then {
            it[PinList].unsafeCast<Array<Json>?>()
                ?.map(Json::toPin)
        }
        .await()
        .let { it ?: throw Exception("Tribe not found.") }
}