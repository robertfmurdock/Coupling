package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.json.toJson
import com.zegreatrob.coupling.model.tribe.KtTribe
import com.zegreatrob.coupling.model.tribe.TribeSave
import com.zegreatrob.coupling.sdk.external.axios.axios
import kotlinx.coroutines.asDeferred
import kotlin.js.Promise

interface SdkTribeSave : TribeSave {
    override suspend fun save(tribe: KtTribe) {
        axios.post("/api/tribes/", tribe.toJson())
            .unsafeCast<Promise<Unit>>()
            .asDeferred()
            .await()
    }
}