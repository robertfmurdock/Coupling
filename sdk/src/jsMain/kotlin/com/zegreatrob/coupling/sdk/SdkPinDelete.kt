package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.repository.pin.PinDelete
import kotlin.js.json

interface SdkPinDelete : PinDelete, GqlSyntax {
    override suspend fun deletePin(tribeId: TribeId, pinId: String) =
        performQuery(
            json(
                "query" to Mutations.deletePin,
                "variables" to json("input" to json("tribeId" to tribeId.value, "pinId" to pinId))
            )
        )
            .data.data.deletePin
            .unsafeCast<Boolean?>() ?: false

}
