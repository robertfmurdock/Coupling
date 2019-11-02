package com.zegreatrob.coupling.client.sdk

import com.zegreatrob.coupling.client.external.axios.axios
import com.zegreatrob.coupling.client.external.axios.getList
import com.zegreatrob.coupling.json.toTribe
import com.zegreatrob.coupling.model.tribe.TribeListGet
import kotlinx.coroutines.asDeferred
import kotlin.js.Json

interface AxiosTribeListGet : TribeListGet {
    override fun getTribesAsync() = axios.getList("/api/tribes")
        .then { it.map(Json::toTribe) }
        .asDeferred()
}