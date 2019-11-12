package com.zegreatrob.coupling.server.entity.player

import com.zegreatrob.coupling.json.toJson
import com.zegreatrob.coupling.json.toPlayer
import com.zegreatrob.coupling.model.player.TribeIdPlayer
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.server.action.player.SavePlayerCommand
import com.zegreatrob.coupling.server.action.player.SavePlayerCommandDispatcher
import com.zegreatrob.coupling.server.entity.tribe.ScopeSyntax
import kotlinx.coroutines.promise
import kotlin.js.Json

interface SavePlayerCommandDispatcherJs : SavePlayerCommandDispatcher, ScopeSyntax {

    @JsName("performSavePlayerCommand")
    fun performSavePlayerCommand(player: Json, tribeId: String) = scope.promise {
        SavePlayerCommand(
            TribeIdPlayer(TribeId(tribeId), player.toPlayer())
        )
            .perform()
            .toJson()
    }

}
