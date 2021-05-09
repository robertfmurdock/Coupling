package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.model.pin.TribeIdPin
import com.zegreatrob.coupling.model.pin.tribeId
import com.zegreatrob.coupling.repository.pin.PinSave
import kotlin.js.json

interface SdkPinSave : PinSave, GqlSyntax {
    override suspend fun save(tribeIdPin: TribeIdPin) {
        performQuery(
            json(
                "query" to Mutations.savePin,
                "variables" to json("input" to tribeIdPin.savePinInput())
            )
        )
    }

    private fun TribeIdPin.savePinInput() = json(
        "tribeId" to tribeId.value,
        "pinId" to element.id,
        "icon" to element.icon,
        "name" to element.name
    )
}
