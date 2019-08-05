package com.zegreatrob.coupling.client.player

import com.zegreatrob.coupling.client.external.axios.axios
import com.zegreatrob.coupling.client.external.axios.getList
import com.zegreatrob.coupling.common.entity.player.Player
import com.zegreatrob.coupling.common.entity.tribe.TribeId
import com.zegreatrob.coupling.common.toPlayer
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.asDeferred
import kotlin.js.Json

interface GetPlayerListSyntax {

    fun TribeId.getPlayerListAsync(): Deferred<List<Player>> = axios.getList("/api/$value/players")
            .then { it.map(Json::toPlayer) }
            .asDeferred()

}