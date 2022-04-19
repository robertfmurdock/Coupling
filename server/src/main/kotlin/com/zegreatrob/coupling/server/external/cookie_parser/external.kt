package com.zegreatrob.coupling.server.external.cookie_parser

import com.zegreatrob.coupling.server.external.express.Handler

@JsModule("cookie-parser")
@JsNonModule
external fun cookieParser(): Handler
