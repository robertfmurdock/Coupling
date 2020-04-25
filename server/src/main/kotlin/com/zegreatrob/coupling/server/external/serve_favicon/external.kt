package com.zegreatrob.coupling.server.external.serve_favicon

import com.zegreatrob.coupling.server.external.express.Handler

@JsModule("serve-favicon")
@JsNonModule
external fun favicon(iconPath: String): Handler