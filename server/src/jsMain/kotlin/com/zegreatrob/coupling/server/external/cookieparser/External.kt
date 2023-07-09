package com.zegreatrob.coupling.server.external.cookieparser

import com.zegreatrob.coupling.server.external.express.Handler

@JsModule("cookie-parser")
@JsNonModule
external fun cookieParser(): Handler
