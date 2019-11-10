package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.json.toJson
import com.zegreatrob.coupling.model.tribe.KtTribe
import com.zegreatrob.coupling.model.tribe.TribeSave
import com.zegreatrob.coupling.sdk.external.axios.postAsync

interface SdkTribeSave : TribeSave, AxiosSyntax {
    override suspend fun save(tribe: KtTribe) = axios.postAsync<Unit>("/api/tribes/", tribe.toJson()).await()
}
