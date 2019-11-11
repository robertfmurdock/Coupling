package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.json.toJson
import com.zegreatrob.coupling.model.pin.PinSaver
import com.zegreatrob.coupling.model.pin.TribeIdPin

interface SdkPinSaver : PinSaver, AxiosSyntax {
    override suspend fun save(tribeIdPin: TribeIdPin) {
        val (tribeId, pin) = tribeIdPin
        axios.postAsync<Unit>("/api/${tribeId.value}/pins/", pin.toJson())
            .await()
    }
}
