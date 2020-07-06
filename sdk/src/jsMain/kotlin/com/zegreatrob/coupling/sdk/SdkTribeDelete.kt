package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.repository.tribe.TribeDelete
import kotlinx.coroutines.asDeferred
import kotlin.js.Promise

interface SdkTribeDelete : TribeDelete, AxiosSyntax {

    override suspend fun delete(tribeId: TribeId): Boolean {
        axios.delete("/api/tribes/${tribeId.value}")
            .unsafeCast<Promise<Unit>>()
            .asDeferred()
            .await()
        return true
    }

}