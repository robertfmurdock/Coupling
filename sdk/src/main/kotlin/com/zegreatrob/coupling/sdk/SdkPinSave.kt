package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.model.pin.TribeIdPin
import com.zegreatrob.coupling.model.pin.tribeId
import com.zegreatrob.coupling.repository.pin.PinSave

interface SdkPinSave : PinSave, GqlSyntax {
    override suspend fun save(tribeIdPin: TribeIdPin): Unit = performQuery(Mutations.savePin, tribeIdPin.savePinInput())
        .unsafeCast<Unit>()

    private fun TribeIdPin.savePinInput() = mapOf(
        "tribeId" to tribeId.value,
        "pinId" to element.id,
        "icon" to element.icon,
        "name" to element.name
    )
}
