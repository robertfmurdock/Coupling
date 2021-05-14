package com.zegreatrob.coupling.e2e.test.external.webdriverio

object ConfigForm : StyleSyntax {
    override val styles = loadStyles("ConfigForm")
    val saveButton by getting()
    val deleteButton by getting()
}
