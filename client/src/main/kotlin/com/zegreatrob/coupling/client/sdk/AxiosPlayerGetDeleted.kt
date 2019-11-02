package com.zegreatrob.coupling.client.sdk

import com.zegreatrob.coupling.client.external.axios.axios
import com.zegreatrob.coupling.client.external.axios.getList
import com.zegreatrob.coupling.json.toPlayer
import com.zegreatrob.coupling.model.player.PlayerGetDeleted
import com.zegreatrob.coupling.model.tribe.TribeId
import kotlinx.coroutines.asDeferred
import kotlin.js.Json

interface AxiosPlayerGetDeleted : PlayerGetDeleted {
    override fun getDeletedAsync(tribeId: TribeId) = axios.getList("/api/${tribeId.value}/players/retired")
        .then { it.map(Json::toPlayer) }
        .asDeferred()
}