package com.zegreatrob.coupling.server.express.route

import com.zegreatrob.coupling.server.external.express.RouterParams

fun routerParams(mergeParams: Boolean = false) = object : RouterParams {
    override val mergeParams: Boolean get() = mergeParams
}