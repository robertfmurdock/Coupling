package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.repository.pin.PinDelete

interface SdkPinDelete : PinDelete, GqlSyntax, GraphQueries {
    override suspend fun deletePin(tribeId: PartyId, pinId: String) =
        doQuery(mutations.deletePin, deletePinInput(tribeId, pinId), "deletePin", ::passThrough) ?: false
    private fun passThrough(it: Boolean?): Boolean? = it
    private fun deletePinInput(tribeId: PartyId, pinId: String) = mapOf("tribeId" to tribeId.value, "pinId" to pinId)
}
