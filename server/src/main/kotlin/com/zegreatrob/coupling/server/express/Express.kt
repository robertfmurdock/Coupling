package com.zegreatrob.coupling.server.express

import com.zegreatrob.coupling.server.external.express.Express

val Express.env get() = get("env").unsafeCast<String>()

val Express.port get() = get("port").unsafeCast<Int?>() ?: 0
