package com.zegreatrob.coupling.server.express.middleware

import com.zegreatrob.coupling.server.express.Config
import com.zegreatrob.coupling.server.external.express_session.session
import com.zegreatrob.coupling.server.useInMemory
import kotlin.js.json

fun session() = session(
    json(
        "secret" to Config.secret,
        "resave" to true,
        "saveUninitialized" to true,
    ).let {
        if (useInMemory())
            it
        else {
            it.add(json("store" to sessionStore()))
        }
    }
)