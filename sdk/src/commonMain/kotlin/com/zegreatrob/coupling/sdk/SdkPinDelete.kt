package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.repository.pin.PinDelete

interface SdkPinDelete : PinDelete, GqlSyntax, GraphQueries {
    override suspend fun deletePin(partyId: PartyId, pinId: String) =
        doQuery(mutations.deletePin, deletePinInput(partyId, pinId), "deletePin", ::passThrough) ?: false
    private fun passThrough(it: Boolean?): Boolean? = it
    private fun deletePinInput(partyId: PartyId, pinId: String) = mapOf("partyId" to partyId.value, "pinId" to pinId)
}
