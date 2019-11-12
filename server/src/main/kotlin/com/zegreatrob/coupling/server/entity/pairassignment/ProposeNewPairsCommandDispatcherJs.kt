package com.zegreatrob.coupling.server.entity.pairassignment

import com.zegreatrob.coupling.json.toJson
import com.zegreatrob.coupling.json.toPlayer
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.server.action.pairassignmentdocument.ProposeNewPairsCommand
import com.zegreatrob.coupling.server.action.pairassignmentdocument.ProposeNewPairsCommandDispatcher
import com.zegreatrob.coupling.server.entity.tribe.ScopeSyntax
import kotlinx.coroutines.promise
import kotlin.js.Json

interface ProposeNewPairsCommandDispatcherJs : ProposeNewPairsCommandDispatcher, ScopeSyntax {
    @JsName("performProposeNewPairsCommand")
    fun performProposeNewPairsCommand(tribeId: String, players: Array<Json>) = scope.promise {
        ProposeNewPairsCommand(TribeId(tribeId), players.map(Json::toPlayer))
            .perform()
            .toJson()
    }
}
