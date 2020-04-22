@file:JsModule("passport-custom")
@file:JsNonModule
package com.zegreatrob.coupling.server.external.passportcustom

import com.zegreatrob.coupling.server.external.express.Request

external class Strategy(func: (Request, (dynamic, dynamic) -> Unit) -> Unit)