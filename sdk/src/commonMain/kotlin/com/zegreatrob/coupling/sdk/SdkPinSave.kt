package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.model.pin.TribeIdPin
import com.zegreatrob.coupling.model.pin.tribeId
import com.zegreatrob.coupling.repository.pin.PinSave

interface SdkPinSave : PinSave, GqlSyntax {
    override suspend fun save(tribeIdPin: TribeIdPin) {
        doQuery(Mutations.savePin, tribeIdPin.savePinInput())
    }

    private fun TribeIdPin.savePinInput() = mapOf(
        "tribeId" to tribeId.value,
        "pinId" to element.id,
        "icon" to element.icon,
        "name" to element.name
    )
}
