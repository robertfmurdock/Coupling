package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.json.toJson
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.model.tribe.TribeSave

interface SdkTribeSave : TribeSave, AxiosSyntax {
    override suspend fun save(tribe: Tribe) = axios.postAsync<Unit>("/api/tribes/", tribe.toJson()).await()
}
