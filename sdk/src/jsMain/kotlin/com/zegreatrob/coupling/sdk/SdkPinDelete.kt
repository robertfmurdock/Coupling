package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.repository.pin.PinDelete
import kotlinx.coroutines.asDeferred
import kotlin.js.Promise

interface SdkPinDelete : PinDelete, AxiosSyntax {
    override suspend fun deletePin(tribeId: TribeId, pinId: String) = try {
        axios.delete("/api/tribes/${tribeId.value}/pins/$pinId")
            .unsafeCast<Promise<Unit>>()
            .asDeferred()
            .await()
        true
    } catch (error: Throwable) {
        false
    }
}
