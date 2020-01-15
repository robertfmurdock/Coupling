package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.json.toJson
import com.zegreatrob.coupling.model.pin.TribeIdPin
import com.zegreatrob.coupling.repository.pin.PinSave

interface SdkPinSave : PinSave, AxiosSyntax {
    override suspend fun save(tribeIdPin: TribeIdPin) {
        val (tribeId, pin) = tribeIdPin
        axios.postAsync<Unit>("/api/tribes/${tribeId.value}/pins/", pin.toJson())
            .await()
    }
}
