package com.zegreatrob.coupling.cli

import com.zegreatrob.coupling.cli.external.open.open

actual fun openBrowser(uri: String) {
    open(uri)
}
