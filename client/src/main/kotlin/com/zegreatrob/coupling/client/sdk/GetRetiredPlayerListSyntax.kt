package com.zegreatrob.coupling.client.sdk

import com.zegreatrob.coupling.client.external.axios.axios
import com.zegreatrob.coupling.client.external.axios.getList
import com.zegreatrob.coupling.common.entity.tribe.TribeId
import com.zegreatrob.coupling.common.toPlayer
import kotlinx.coroutines.asDeferred
import kotlin.js.Json

interface GetRetiredPlayerListSyntax {

    fun TribeId.getRetiredPlayerListAsync() = axios.getList("/api/$value/players/retired")
        .then { it.map(Json::toPlayer) }
        .asDeferred()

}