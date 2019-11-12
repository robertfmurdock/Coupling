package com.zegreatrob.coupling.server.entity.tribe

import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.server.external.express.Request

interface RequestTribeIdSyntax {
    fun Request.tribeId() = TribeId(params["tribeId"].toString())
}