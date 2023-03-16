package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.model.party.PartyElement
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.pin.partyId
import com.zegreatrob.coupling.repository.pin.PinSave

interface SdkPinSave : PinSave, GqlSyntax, GraphQueries {
    override suspend fun save(partyPin: PartyElement<Pin>) {
        doQuery(mutations.savePin, partyPin.savePinInput())
    }

    private fun PartyElement<Pin>.savePinInput() =
        mapOf(
            "partyId" to partyId.value,
            "pinId" to element.id,
            "icon" to element.icon,
            "name" to element.name,
        )
}
