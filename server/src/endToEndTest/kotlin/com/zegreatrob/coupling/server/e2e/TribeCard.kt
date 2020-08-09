package com.zegreatrob.coupling.server.e2e

object TribeCard : StyleSyntax {
    override val styles = loadStyles("tribe/TribeCard")
    val header by getting()
}
