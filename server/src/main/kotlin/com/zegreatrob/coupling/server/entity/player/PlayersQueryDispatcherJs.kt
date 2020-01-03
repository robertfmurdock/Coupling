package com.zegreatrob.coupling.server.entity.player

import com.zegreatrob.coupling.json.toJsonArray
import com.zegreatrob.coupling.server.action.player.PlayersQuery
import com.zegreatrob.coupling.server.action.player.PlayersQueryDispatcher

interface PlayersQueryDispatcherJs : PlayersQueryDispatcher {

    suspend fun performPlayerListQueryGQL() = PlayersQuery
        .perform()
        .toJsonArray()

}
