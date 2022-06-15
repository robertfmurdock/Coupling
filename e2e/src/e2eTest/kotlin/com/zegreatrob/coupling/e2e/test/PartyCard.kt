package com.zegreatrob.coupling.e2e.test

object PartyCard : StyleSyntax {
    override val styles = loadStyles("party/TribeCard")
    val header by getting()
}
