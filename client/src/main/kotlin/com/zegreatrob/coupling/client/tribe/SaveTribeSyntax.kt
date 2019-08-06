package com.zegreatrob.coupling.client.tribe

import com.zegreatrob.coupling.client.external.axios.axios
import com.zegreatrob.coupling.common.entity.tribe.KtTribe
import com.zegreatrob.coupling.common.toJson
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.asDeferred
import kotlin.js.Promise

interface SaveTribeSyntax {

    fun KtTribe.saveAsync(): Deferred<Unit> = axios.post("/api/tribes/", toJson())
            .unsafeCast<Promise<Unit>>()
            .asDeferred()

}

