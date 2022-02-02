package com.zegreatrob.coupling.e2e.test

object TribeCard : StyleSyntax {
    override val styles = loadStyles("tribe/TribeCard")
    val header by getting()
}
