package com.zegreatrob.coupling.server.entity.pin

import com.zegreatrob.coupling.server.external.express.Request

interface RequestPinIdSyntax {
    fun Request.pinId() = params["pinId"].toString()
}
