package com.zegreatrob.coupling.server.external.on_finished

import com.zegreatrob.coupling.server.external.express.Response

@JsModule("on-finished")
@JsNonModule
external fun onFinished(response: Response, callback: dynamic)