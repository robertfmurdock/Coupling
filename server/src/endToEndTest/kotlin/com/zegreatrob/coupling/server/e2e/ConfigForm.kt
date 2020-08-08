package com.zegreatrob.coupling.server.e2e

object ConfigForm : StyleSyntax {
    override val styles = loadStyles("ConfigForm")
    val saveButton by getting()
    val deleteButton by getting()
}
