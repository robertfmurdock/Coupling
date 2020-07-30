@file:JsModule("passport-custom")


package com.zegreatrob.coupling.server.external.passportcustom

import com.zegreatrob.coupling.server.external.express.Request

external class Strategy(func: (Request, (dynamic, dynamic) -> Unit) -> Unit)