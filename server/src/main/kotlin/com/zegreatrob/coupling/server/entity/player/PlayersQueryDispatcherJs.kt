package com.zegreatrob.coupling.server.entity.player

import com.zegreatrob.coupling.json.toJsonArray
import com.zegreatrob.coupling.server.action.player.PlayersQuery
import com.zegreatrob.coupling.server.action.player.PlayersQueryDispatcher
import com.zegreatrob.coupling.server.entity.tribe.ScopeSyntax

interface PlayersQueryDispatcherJs : PlayersQueryDispatcher, ScopeSyntax {

    suspend fun performPlayerListQueryGQL() = PlayersQuery
        .perform()
        .toJsonArray()

}
