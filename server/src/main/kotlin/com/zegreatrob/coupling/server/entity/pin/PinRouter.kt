package com.zegreatrob.coupling.server.entity.pin

import com.zegreatrob.coupling.server.express.route.routerParams
import com.zegreatrob.coupling.server.external.express.Router

val pinRouter by lazy {
    Router(routerParams(mergeParams = true)).apply {
    }
}
