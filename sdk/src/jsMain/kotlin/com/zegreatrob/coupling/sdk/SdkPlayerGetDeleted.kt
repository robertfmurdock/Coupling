package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.json.toPlayer
import com.zegreatrob.coupling.model.player.PlayerGetDeleted
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.sdk.external.axios.getList
import kotlinx.coroutines.asDeferred
import kotlin.js.Json

interface SdkPlayerGetDeleted : PlayerGetDeleted, AxiosSyntax {
    override fun getDeletedAsync(tribeId: TribeId) = axios.getList("/api/${tribeId.value}/players/retired")
        .then { it.map(Json::toPlayer) }
        .asDeferred()
}