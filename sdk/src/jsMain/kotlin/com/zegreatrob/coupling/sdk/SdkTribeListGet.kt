package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.json.toTribe
import com.zegreatrob.coupling.model.tribe.TribeListGet
import com.zegreatrob.coupling.sdk.external.axios.getList
import kotlinx.coroutines.asDeferred
import kotlin.js.Json

interface SdkTribeListGet : TribeListGet, AxiosSyntax {
    override fun getTribesAsync() = axios.getList("/api/tribes")
        .then { it.map(Json::toTribe) }
        .asDeferred()
}