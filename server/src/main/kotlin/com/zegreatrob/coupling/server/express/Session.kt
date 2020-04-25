package com.zegreatrob.coupling.server.express

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