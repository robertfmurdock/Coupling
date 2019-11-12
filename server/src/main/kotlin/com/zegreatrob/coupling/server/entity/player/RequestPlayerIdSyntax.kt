package com.zegreatrob.coupling.server.entity.player

import com.zegreatrob.coupling.server.external.express.Request

interface RequestPlayerIdSyntax {
    fun Request.playerId() = params["playerId"].toString()
}
