package com.zegreatrob.coupling.client.sdk

import com.zegreatrob.coupling.client.sdk.axios.axios.axios
import com.zegreatrob.coupling.client.sdk.axios.axios.getList
import com.zegreatrob.coupling.json.toPlayer
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.player.PlayerGetter
import com.zegreatrob.coupling.model.tribe.TribeId
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.asDeferred
import kotlin.js.Json

interface SdkPlayerGetter : PlayerGetter {
    override fun getPlayersAsync(tribeId: TribeId): Deferred<List<Player>> =
        axios.getList("/api/${tribeId.value}/players")
            .then { it.map(Json::toPlayer) }
            .asDeferred()
}