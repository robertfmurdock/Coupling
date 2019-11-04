package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.sdk.external.axios.Axios

interface AxiosSyntax {
    val axios: Axios get() = com.zegreatrob.coupling.sdk.external.axios.axios
}
