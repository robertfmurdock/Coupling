package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.repository.pin.PinDelete
import com.zegreatrob.minjson.at
import kotlin.js.Json
import kotlin.js.json

interface SdkPinDelete : PinDelete, GqlSyntax {
    override suspend fun deletePin(tribeId: TribeId, pinId: String) = performQuery(
        json(
            "query" to Mutations.deletePin,
            "variables" to json("input" to json("tribeId" to tribeId.value, "pinId" to pinId))
        )
    ).unsafeCast<Json>()
        .at("/data/data/deletePin") ?: false
}
