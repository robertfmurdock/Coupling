package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.json.pinJsonKeys
import com.zegreatrob.coupling.json.toPins
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.repository.pin.PinGetter
import kotlinx.coroutines.await
import kotlin.js.Json
import kotlin.js.json

interface SdkPinGetter : PinGetter, AxiosSyntax {
    override suspend fun getPins(tribeId: TribeId): List<Pin> = axios.post(
        "/api/graphql", json(
            "query" to "{ pinList(tribeId: \"${tribeId.value}\") {${pinJsonKeys.joinToString(",")}} }"
        )
    )
        .then<List<Pin>?> {
            it.data.unsafeCast<Json>()["data"]
                .unsafeCast<Json>()["pinList"]
                .unsafeCast<Array<Json>?>()
                ?.toPins()
        }
        .await()
        .let { it ?: throw Exception("Tribe not found.") }
}