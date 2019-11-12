package com.zegreatrob.coupling.server.entity.player

import com.zegreatrob.coupling.json.toJson
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.server.action.player.PlayersQuery
import com.zegreatrob.coupling.server.action.player.PlayersQueryDispatcher
import com.zegreatrob.coupling.server.entity.tribe.ScopeSyntax
import kotlinx.coroutines.promise

interface PlayersQueryDispatcherJs : PlayersQueryDispatcher, ScopeSyntax {
    @JsName("performPlayersQuery")
    fun performPlayersQuery(tribeId: String) = scope.promise {
        PlayersQuery(TribeId(tribeId))
            .perform()
            .map { it.toJson() }
            .toTypedArray()
    }
}
