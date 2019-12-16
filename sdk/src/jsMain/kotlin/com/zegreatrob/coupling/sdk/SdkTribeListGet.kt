package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.json.toTribe
import com.zegreatrob.coupling.repository.tribe.TribeListGet
import com.zegreatrob.coupling.sdk.external.axios.getList
import kotlinx.coroutines.await
import kotlin.js.Json

interface SdkTribeListGet : TribeListGet, AxiosSyntax {
    override suspend fun getTribes() = axios.getList("/api/tribes")
        .then { it.map(Json::toTribe) }
        .await()
}