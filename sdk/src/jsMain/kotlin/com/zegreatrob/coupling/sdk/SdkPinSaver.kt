package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.json.toJson
import com.zegreatrob.coupling.model.pin.PinSaver
import com.zegreatrob.coupling.model.pin.TribeIdPin
import kotlinx.coroutines.asDeferred
import kotlin.js.Promise

interface SdkPinSaver : PinSaver, AxiosSyntax {
    override suspend fun save(tribeIdPin: TribeIdPin) {
        val (tribeId, pin) = tribeIdPin
        axios.post("/api/${tribeId.value}/pins/", pin.toJson())
            .unsafeCast<Promise<Unit>>()
            .asDeferred()
            .await()
    }
}
