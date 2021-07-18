package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.repository.pin.PinDelete
import com.zegreatrob.minjson.at
import kotlin.js.Json

interface SdkPinDelete : PinDelete, GqlSyntax {
    override suspend fun deletePin(tribeId: TribeId, pinId: String) =
        performQuery(Mutations.deletePin, deletePinInput(tribeId, pinId))
            .unsafeCast<Json>()
            .at("/data/data/deletePin") ?: false

    private fun deletePinInput(tribeId: TribeId, pinId: String) = mapOf("tribeId" to tribeId.value, "pinId" to pinId)
}
