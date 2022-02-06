package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.repository.pin.PinDelete

interface SdkPinDelete : PinDelete, GqlSyntax, GraphQueries {
    override suspend fun deletePin(tribeId: TribeId, pinId: String) =
        doQuery(mutations.deletePin, deletePinInput(tribeId, pinId), "deletePin", ::passThrough) ?: false
    private fun passThrough(it: Boolean?): Boolean? = it
    private fun deletePinInput(tribeId: TribeId, pinId: String) = mapOf("tribeId" to tribeId.value, "pinId" to pinId)
}
