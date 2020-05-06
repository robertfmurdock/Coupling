package com.zegreatrob.coupling.server.express.middleware

import com.zegreatrob.coupling.server.express.Config
import com.zegreatrob.coupling.server.external.express_session.session
import kotlin.js.json

fun session() = session(
    json(
        "secret" to Config.secret,
        "resave" to true,
        "saveUninitialized" to true,
        "store" to sessionStore()
    )
)